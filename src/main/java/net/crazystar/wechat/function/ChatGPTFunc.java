package net.crazystar.wechat.function;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.session.WxSession;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import net.crazystar.wechat.builder.TextBuilder;
import net.crazystar.wechat.builder.VoiceBuilder;
import net.crazystar.wechat.service.cognitive.CognitiveService;
import net.crazystar.wechat.service.openai.ChatGptService;
import net.crazystar.wechat.service.openai.entity.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static net.crazystar.wechat.constant.SessionConstants.*;
import static net.crazystar.wechat.function.FuncConstants.CHAT_GPT;

@Service(CHAT_GPT)
@Slf4j
public class ChatGPTFunc implements IFunction {
    private static final int MAX_MESSAGES = 20;
    private static final int TEXT2VOICE_THRESHOLD = 250;
    private static final String WELCOME_MSG = "欢迎体验ChatGPT功能，输入“退出”结束体验。可能会出现响应时间比较长，需要重新输入的情况。";
    private static final String TIMEOUT_MSG = "获取结果中，请稍等几秒后复制原文再说一遍。";
    private static final int VOICE_TIMEOUT_TIME = 3000;
    private static final int TEXT_TIMEOUT_TIME = 4500;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final CognitiveService cognitiveService;
    private final ChatGptService chatGptService;

    public ChatGPTFunc(CognitiveService cognitiveService,
                       ChatGptService chatGptService) {
        this.cognitiveService = cognitiveService;
        this.chatGptService = chatGptService;
    }

    @Override
    public WxMpXmlOutMessage exec(FunctionContext functionContext) {
        WxMpService weixinService = functionContext.getWxMpService();
        WxMpXmlMessage wxMessage = functionContext.getWxMessage();
        String fromUser = wxMessage.getFromUser();
        WxSession session = functionContext.getSessionManager().getSession(fromUser);
        String content = wxMessage.getContent();
        if (enter(content)) {
            return new TextBuilder().build(WELCOME_MSG, wxMessage, weixinService);
        }
        boolean voiceMsg = false;
        if (WxConsts.XmlMsgType.VOICE.equals(wxMessage.getMsgType()) && StringUtils.isNotEmpty(wxMessage.getRecognition())) {
            voiceMsg = true;
            content = wxMessage.getRecognition();
        }

        @SuppressWarnings("unchecked")
        Map<String, String> lastResp = (Map<String, String>) session.getAttribute(ATTR_CHAT_TIMEOUT_RESP_MAP);
        if (lastResp == null) {
            lastResp = new ConcurrentHashMap<>(2);
        }
        String resp = lastResp.remove(content);
        if (StringUtils.isNotEmpty(resp)) {
            return reply(weixinService, wxMessage, resp, voiceMsg);
        }

        @SuppressWarnings("unchecked")
        List<Message> chatMessages = (List<Message>) session.getAttribute(ATTR_CHAT_GPT_MESSAGES);
        if (CollectionUtils.isEmpty(chatMessages)) {
            chatMessages = new ArrayList<>();
        }

        if (chatMessages.size() > 0) {
            Message message = chatMessages.get(chatMessages.size() - 1);
            if (content.equals(message.getContent())) {
                return new TextBuilder().build(TIMEOUT_MSG, wxMessage, weixinService);
            }
        }

        if (chatMessages.size() > MAX_MESSAGES) {
            chatMessages = new ArrayList<>(chatMessages.subList(chatMessages.size() - MAX_MESSAGES, chatMessages.size()));
        }
        chatMessages.add(Message.user(content));


        List<Message> finalChatMessages = chatMessages;
        Future<String> future = executor.submit(() -> chatGptService.chat(finalChatMessages, fromUser));

        try {
            String chat = future.get(voiceMsg ? VOICE_TIMEOUT_TIME : TEXT_TIMEOUT_TIME, TimeUnit.MILLISECONDS);
            log.info("fromUser: {}, conversation: {}", fromUser, finalChatMessages);
            session.setAttribute(ATTR_CHAT_GPT_MESSAGES, finalChatMessages);

            return reply(weixinService, wxMessage, chat, voiceMsg);
        } catch (TimeoutException e) {
            Map<String, String> finalLastResp = lastResp;
            String finalContent = content;
            executor.execute(() -> {
                try {
                    finalLastResp.put(finalContent, future.get());
                    log.info("fromUser: {}, conversation: {}", fromUser, finalChatMessages);
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            });
            session.setAttribute(ATTR_CHAT_TIMEOUT_RESP_MAP, finalLastResp);
            return new TextBuilder().build(TIMEOUT_MSG, wxMessage, weixinService);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean enter(String msg) {
        return CHAT_GPT.equalsIgnoreCase(msg);
    }

    @Override
    public boolean exit(String msg) {
        return "退出".equals(msg);
    }

    @Override
    public String name() {
        return CHAT_GPT;
    }

    @Override
    public boolean hasSession() {
        return true;
    }

    @Override
    public void cleanUp(WxSession session) {
        session.removeAttribute(ATTR_CURRENT_FUNC);
        session.removeAttribute(ATTR_CHAT_GPT_MESSAGES);
        session.removeAttribute(ATTR_CHAT_TIMEOUT_RESP_MAP);
    }

    private WxMpXmlOutMessage reply(WxMpService weixinService, WxMpXmlMessage wxMessage, String content, boolean voice) {
        if (voice && content.length() < TEXT2VOICE_THRESHOLD) {
            try {
                return new VoiceBuilder().build(convert2VoiceAndUpload(weixinService, content), wxMessage, weixinService);
            } catch (Exception e) {
                log.error("text2voice or upload error. ", e);
            }
        }
        return new TextBuilder().build(content, wxMessage, weixinService);
    }

    private String convert2VoiceAndUpload(WxMpService weixinService, String text) throws Exception {
        File file = text2voice(text);
        long start = System.currentTimeMillis();
        WxMediaUploadResult res = weixinService.getMaterialService().mediaUpload(WxConsts.MediaFileType.VOICE, file);
        log.info("upload tts used time: {}", System.currentTimeMillis() - start);
        // TODO: clean file
//        file.delete();
        return res.getMediaId();
    }

    private File text2voice(String text) throws Exception {
        return cognitiveService.tts(text);
    }

}
