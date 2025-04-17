package com.jobscheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Listen for Kafka job notification messages and forward them to WebSocket clients
     */
    @KafkaListener(topics = "job-notifications", groupId = "websocket-forwarder")
    public void forwardJobNotifications(String message) {
        log.info("Forwarding job notification to WebSocket clients: {}", message);
        messagingTemplate.convertAndSend("/topic/job-notifications", message);
    }
} 