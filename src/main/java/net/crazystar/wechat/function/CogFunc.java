package net.crazystar.wechat.function;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSession;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import net.crazystar.wechat.builder.TextBuilder;
import org.springframework.stereotype.Service;

import static net.crazystar.wechat.function.FuncConstants.COG;

@Service(COG)
@Slf4j
public class CogFunc implements IFunction {

    @Override
    public WxMpXmlOutMessage exec(FunctionContext functionContext) {
        
        String content = "。";
        return new TextBuilder().build(content, functionContext.getWxMessage(), functionContext.getWxMpService());
    }

    @Override
    public boolean enter(String msg) {
        return msg != null && msg.contains("口语练习");
    }

    @Override
    public boolean exit(String msg) {
        return true;
    }

    @Override
    public String name() {
        return COG;
    }

    @Override
    public boolean hasSession() {
        return true;
    }

    @Override
    public void cleanUp(WxSession wxSession) {

    }
}
