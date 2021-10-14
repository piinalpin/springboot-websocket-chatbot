package com.piinalpin.websocketserver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.piinalpin.websocketserver.domain.dto.MessageDto;
import com.piinalpin.websocketserver.domain.dto.MessageResponse;
import com.piinalpin.websocketserver.domain.dto.RiddlesDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${chatbot.url}")
    private String chatbotUrl;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        log.info("Payload: " + message.getPayload());
        MessageDto messageDto = mapper.readValue(message.getPayload(), MessageDto.class);

        if (StringUtils.isEmpty(messageDto.getMessage())) return;

        ResponseEntity<MessageResponse> responseEntity;

        try {
            responseEntity = restTemplate.postForEntity(chatbotUrl, messageDto, MessageResponse.class);
        } catch (Exception e) {
            log.info("Session ID: " + session.getId());
            log.error("Happened error", e);
            session.sendMessage(new TextMessage(mapper.writeValueAsString(MessageDto.builder().message("Maaf bot tidak tersedia saat ini.").build())));
            return;
        }

        MessageResponse messageResponse = responseEntity.getBody();

        if (Objects.requireNonNull(messageResponse).getType().equalsIgnoreCase("riddles")) {
            RiddlesDto dto = mapper.convertValue(messageResponse.getData(), RiddlesDto.class);
            log.info("RiddlesDto:: " + dto);
            TextMessage question = new TextMessage(mapper.writeValueAsString(MessageDto.builder().message(dto.getQuestion()).build()));
            TextMessage answer = new TextMessage(mapper.writeValueAsString(MessageDto.builder().message(dto.getAnswer()).build()));
            session.sendMessage(question);
            TimeUnit.SECONDS.sleep(1);
            session.sendMessage(answer);
        } else {
            TextMessage response = new TextMessage(mapper.writeValueAsString(messageResponse.getData()));
            log.info("Response: " + response.getPayload());
            session.sendMessage(response);
        }
    }
}
