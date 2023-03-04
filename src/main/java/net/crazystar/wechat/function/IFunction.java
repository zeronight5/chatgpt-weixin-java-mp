package net.crazystar.wechat.function;

import me.chanjar.weixin.common.session.WxSession;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

public interface IFunction {
    WxMpXmlOutMessage exec(FunctionContext functionContext);

    boolean enter(String msg);

    boolean exit(String msg);

    String name();

    boolean hasSession();

    void cleanUp(WxSession wxSession);


}
