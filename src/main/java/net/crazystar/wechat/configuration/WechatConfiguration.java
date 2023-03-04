package net.crazystar.wechat.configuration;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import net.crazystar.wechat.handler.LogHandler;
import net.crazystar.wechat.handler.MsgHandler;
import net.crazystar.wechat.handler.SubscribeHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

import static me.chanjar.weixin.common.api.WxConsts.EventType.SUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;
@Slf4j
@Configuration
@EnableConfigurationProperties(WxMpProperties.class)
public class WechatConfiguration {

    private final MsgHandler msgHandler;
    private final LogHandler logHandler;
    private final SubscribeHandler subscribeHandler;
    private final WxMpProperties wxMpProperties;

    public WechatConfiguration(MsgHandler msgHandler, LogHandler logHandler,
                               SubscribeHandler subscribeHandler, WxMpProperties wxMpProperties) {
        this.msgHandler = msgHandler;
        this.logHandler = logHandler;
        this.subscribeHandler = subscribeHandler;
        this.wxMpProperties = wxMpProperties;
    }

    @Bean
    public WxMpService wxMpService() {
        final List<WxMpProperties.MpConfig> configs = this.wxMpProperties.getConfigs();
        if (configs == null) {
            throw new RuntimeException("没有配置微信公众号");
        }

        WxMpService service = new WxMpServiceImpl();
        service.setMultiConfigStorages(configs
                .stream().map(config -> {
                    WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
                    configStorage.setAppId(config.getAppId());
                    configStorage.setSecret(config.getSecret());
                    configStorage.setToken(config.getToken());
                    configStorage.setAesKey(config.getAesKey());
                    return configStorage;
                }).collect(Collectors.toMap(WxMpDefaultConfigImpl::getAppId, a -> a, (o, n) -> o)));
        return service;
    }

    @Bean
    public WxMpMessageRouter messageRouter(WxMpService wxMpService) {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);

        newRouter.rule().handler(this.logHandler).next();

        // 关注事件
        newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(this.subscribeHandler).end();

        // 默认
        newRouter.rule().async(false).handler(this.msgHandler).end();

        return newRouter;
    }
}
