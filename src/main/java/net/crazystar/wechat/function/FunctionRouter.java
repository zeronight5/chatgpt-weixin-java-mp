package net.crazystar.wechat.function;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.session.WxSession;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import net.crazystar.wechat.builder.TextBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

import static net.crazystar.wechat.constant.MsgConstants.DEFAULT_RESP;
import static net.crazystar.wechat.constant.MsgConstants.EXIT_FUNCTION;
import static net.crazystar.wechat.constant.SessionConstants.ATTR_CURRENT_FUNC;

@Service
@Slf4j
public class FunctionRouter {
    @Resource
    private Map<String, IFunction> functions;

    public WxMpXmlOutMessage route(WxMpXmlMessage wxMessage,
                                   Map<String, Object> context,
                                   WxMpService wxMpService,
                                   WxSessionManager sessionManager) {
        String content = StringUtils.trimToNull(wxMessage.getContent());
        if (StringUtils.isEmpty(content) && (WxConsts.XmlMsgType.VOICE.equals(wxMessage.getMsgType())
                && StringUtils.isEmpty(wxMessage.getRecognition()))) {
            return null;
        }

        FunctionContext functionContext = FunctionContext.builder()
                .context(context)
                .sessionManager(sessionManager)
                .wxMpService(wxMpService)
                .wxMessage(wxMessage)
                .build();
        WxSession session = sessionManager.getSession(wxMessage.getFromUser());
        String currentFunc = (String) session.getAttribute(ATTR_CURRENT_FUNC);
        if (StringUtils.isNotEmpty(currentFunc)) {
            IFunction function = functions.get(currentFunc);
            if (!function.exit(wxMessage.getContent())) {
                return function.exec(functionContext);
            } else {
                session.removeAttribute(ATTR_CURRENT_FUNC);
                function.cleanUp(session);
                return new TextBuilder().build(EXIT_FUNCTION, wxMessage, wxMpService);
            }
        }

        IFunction function = null;
        for (Map.Entry<String, IFunction> entry : functions.entrySet()) {
            String name = entry.getKey();
            IFunction func = entry.getValue();
            if (func.enter(wxMessage.getContent())) {
                function = func;
                if (func.hasSession()) {
                    session.setAttribute(ATTR_CURRENT_FUNC, name);
                }
                break;
            }
        }

        return function == null ? new TextBuilder().build(DEFAULT_RESP, wxMessage, wxMpService)
                : function.exec(functionContext);
    }

}
