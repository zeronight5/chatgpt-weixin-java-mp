package net.crazystar.wechat.function;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSession;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import net.crazystar.wechat.builder.TextBuilder;
import org.springframework.stereotype.Service;

import static net.crazystar.wechat.function.FuncConstants.HELP;

@Service(HELP)
@Slf4j
public class HelpFunc implements IFunction {

    @Override
    public WxMpXmlOutMessage exec(FunctionContext functionContext) {
        String content = "功能在持续完善中，当前可用关键词：" +
                "\n“ChatGPT”体验openAI的ChatGPT功能。" +
                "";
        return new TextBuilder().build(content, functionContext.getWxMessage(), functionContext.getWxMpService());
    }

    @Override
    public boolean enter(String msg) {
        return msg != null && msg.contains("帮助");
    }

    @Override
    public boolean exit(String msg) {
        return true;
    }

    @Override
    public String name() {
        return HELP;
    }

    @Override
    public boolean hasSession() {
        return false;
    }

    @Override
    public void cleanUp(WxSession wxSession) {

    }
}
