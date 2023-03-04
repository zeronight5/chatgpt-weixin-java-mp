package net.crazystar.wechat.service.openai;

import lombok.extern.slf4j.Slf4j;
import net.crazystar.wechat.service.openai.entity.ChatReq;
import net.crazystar.wechat.service.openai.entity.ChatResp;
import net.crazystar.wechat.service.openai.entity.Choices;
import net.crazystar.wechat.service.openai.entity.Message;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ChatGptService {
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;

    public ChatGptService(@Qualifier("openAIRestTemplate") RestTemplate restTemplate,
                          @Value("${openai.apiKey}") String apiKey,
                          @Value("${openai.apiHost}") String apiHost) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = "https://" + apiHost + "/v1/chat/completions";
    }

    public String chat(List<Message> messages, String user) {
        long start = System.currentTimeMillis();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        ChatReq chatReq = new ChatReq(messages, user);
        HttpEntity<ChatReq> request = new HttpEntity<>(chatReq, headers);
        ResponseEntity<ChatResp> response = null;
        try {
            response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, ChatResp.class);
        } catch (ResourceAccessException e) {
            log.warn("openai connect error. ", e);
            return "服务不可用。";
        }

        ChatResp resp = response.getBody();
        log.info("cost: {}, req: {}, resp: {}", System.currentTimeMillis() - start, messages, resp);
        List<Choices> choices = resp == null ? Collections.emptyList() : resp.getChoices();
        if (!CollectionUtils.isEmpty(choices)) {
            Message message = choices.get(0).getMessage();
            String result = message.getContent().trim();
            if (!StringUtils.isBlank(result)) {
                messages.add(message);
                return result;
            }
        }
        return "未获取到结果。";
    }

}
