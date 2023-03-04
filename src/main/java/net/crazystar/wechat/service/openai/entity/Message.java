package net.crazystar.wechat.service.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Message {
    @JsonProperty("role")
    private String role;
    @JsonProperty("content")
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static Message assistant(String content) {
        return new Message("assistant", content);
    }

    public static Message user(String content) {
        return new Message("user", content);
    }
}
