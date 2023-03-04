package net.crazystar.wechat.handler;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import net.crazystar.wechat.builder.TextBuilder;
import net.crazystar.wechat.function.FunctionRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Jay
 */
@Component
public class MsgHandler extends AbstractHandler {
    @Autowired
    private FunctionRouter functionRouter;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {

        if (!wxMessage.getMsgType().equals(WxConsts.XmlMsgType.EVENT)) {
            //TODO 可以选择将消息保存到本地
        }

        try {
            return functionRouter.route(wxMessage, context, weixinService, sessionManager);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new TextBuilder().build("服务错误，请稍后重试", wxMessage, weixinService);

    }

}