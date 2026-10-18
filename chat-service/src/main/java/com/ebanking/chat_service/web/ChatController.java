package com.ebanking.chat_service.web;

import com.ebanking.chat_service.dtos.*;
import com.ebanking.chat_service.services.ChatService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService service;

    public ChatController(ChatService service) {
        this.service = service;
    }

    @PostMapping
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request) {
        return service.chat(request.message());
    }
}
