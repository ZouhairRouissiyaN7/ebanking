package com.ebanking.chat_service.discord;

import com.ebanking.chat_service.services.ChatService;

import jakarta.annotation.PreDestroy;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ConditionalOnProperty(name = "app.discord.enabled", havingValue = "true")
public class DiscordCommandListener extends ListenerAdapter {

    private final ChatService chatService;
    private final String guildId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DiscordCommandListener(
            ChatService chatService,
            @Value("${app.discord.guild-id}") String guildId) {
        this.chatService = chatService;
        this.guildId = guildId;
    }

    @Override
    public void onSlashCommandInteraction(
            SlashCommandInteractionEvent event) {
        if (!event.getName().equals("ask")) {
            return;
        }

        if (event.getGuild() == null ||
                !event.getGuild().getId().equals(guildId)) {
            event.reply(
                    "This command is only available in the configured test server.").setEphemeral(true).queue();
            return;
        }

        String question = event.getOption(
                "question",
                OptionMapping::getAsString);

        if (question == null || question.isBlank()) {
            event.reply("Please enter a question.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (question.length() > 1000) {
            event.reply("Questions must be under 1000 characters.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(true).queue(hook -> {

            executor.execute(() -> {
                String reply;

                try {
                    reply = chatService.chat(question).reply();

                    if (reply == null || reply.isBlank()) {
                        reply = "I couldn't generate an answer.";
                    }

                } catch (Exception ex) {
                    reply = "The local AI assistant is unavailable. "
                            + "Please try again later.";
                }

                if (reply.length() > 1900) {
                    reply = reply.substring(0, 1900) + "...";
                }

                hook.editOriginal(reply)
                        .setAllowedMentions(Collections.emptyList())
                        .queue();
            });

        });
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
