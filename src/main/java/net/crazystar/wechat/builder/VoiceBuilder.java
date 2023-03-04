package net.crazystar.wechat.builder;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;

/**
 * @author Jay
 */
public class VoiceBuilder extends AbstractBuilder{
    @Override
    public WxMpXmlOutMessage build(String mediaId, WxMpXmlMessage wxMessage, WxMpService service) {
        return WxMpXmlOutMessage.VOICE().mediaId(mediaId)
                .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
                .build();
    }
}
