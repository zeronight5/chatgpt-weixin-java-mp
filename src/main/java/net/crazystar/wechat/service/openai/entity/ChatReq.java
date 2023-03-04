package net.crazystar.wechat.service.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@NoArgsConstructor
@Data
public class ChatReq {

    @JsonProperty("model")
    private String model = "gpt-3.5-turbo";
    @JsonProperty("messages")
    private List<Message> messages;
    @JsonProperty("user")
    private String user;
    @JsonProperty("stream")
    private Boolean stream;

    public ChatReq(List<Message> messages, String user) {
        this.messages = messages;
        this.user = user;
    }
}
