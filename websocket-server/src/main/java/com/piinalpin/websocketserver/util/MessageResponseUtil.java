package com.piinalpin.websocketserver.util;

import com.piinalpin.websocketserver.domain.dto.MessageResponse;

import java.io.Serializable;

public class MessageResponseUtil {

    private MessageResponseUtil() {}

    public static <T extends Serializable> MessageResponse<T> build(String type, T data) {
        return MessageResponse.<T>builder()
                .type(type)
                .data(data)
                .build();
    }

}
