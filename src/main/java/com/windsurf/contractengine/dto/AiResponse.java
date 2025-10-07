package com.windsurf.contractengine.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AiResponse {

    private String id;
    private String object;
    private int created;
    private String model;
    private List<Message> messages;

    @Data
    public static class Message {
        private String role;
        private String content;
    }

}
