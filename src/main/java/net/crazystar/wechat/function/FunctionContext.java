package net.crazystar.wechat.function;

import lombok.Builder;
import lombok.Getter;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

import java.util.Map;

@Getter
@Builder
public class FunctionContext {
    private WxMpXmlMessage wxMessage;
    private Map<String, Object> context;
    private WxSessionManager sessionManager;
    private WxMpService wxMpService;
}
