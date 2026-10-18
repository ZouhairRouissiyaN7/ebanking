package com.ebanking.chat_service.services;

import com.ebanking.chat_service.dtos.ChatResponse;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                        You are the eBanking virtual assistant.

                        You help users understand:
                        - Current and savings accounts
                        - Deposits and withdrawals
                        - Bank transfers
                        - Transaction history
                        - How to use the eBanking application

                        The application supports:
                        - Customer management for administrators
                        - Current and savings bank accounts
                        - Deposits, withdrawals, and transfers
                        - Account operation history

                        Important rules:
                        - You cannot access the user's actual accounts.
                        - You cannot see balances or transactions.
                        - You cannot execute banking operations.
                        - Never invent personal financial data.
                        - If asked for an actual balance or transaction,
                          explain that live account access is not
                          connected to the chatbot yet.
                        - Never ask users for passwords or JWT tokens.
                        - Respond in the language of the user's message.
                        - Keep responses concise and helpful.
                        """)
                .build();
    }

    public ChatResponse chat(String message) {

        String reply = chatClient.prompt()
                .user(message)
                .call()
                .content();

        if (reply == null || reply.isBlank()) {
            reply = "I couldn't generate a response. Please try again.";
        }

        return new ChatResponse(reply);
    }
}