package com.ebanking.chat_service.discord;

import jakarta.annotation.PreDestroy;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@ConditionalOnProperty(name = "app.discord.enabled", havingValue = "true")
public class DiscordBotManager {

    private final DiscordCommandListener listener;
    private final String token;
    private final String guildId;

    private JDA jda;

    public DiscordBotManager(
            DiscordCommandListener listener,
            @Value("${app.discord.token}") String token,
            @Value("${app.discord.guild-id}") String guildId) {
        this.listener = listener;
        this.token = token;
        this.guildId = guildId;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startBot() throws InterruptedException {

        if (token.isBlank() || guildId.isBlank()) {
            throw new IllegalStateException(
                    "Discord is enabled but token or guild ID is missing");
        }

        jda = JDABuilder.createLight(
                token,
                Collections.emptyList())
                .addEventListeners(listener)
                .build();

        jda.awaitReady();

        Guild guild = jda.getGuildById(guildId);

        if (guild == null) {
            throw new IllegalStateException(
                    "Discord bot is not a member of guild " + guildId);
        }

        guild.upsertCommand(
                Commands.slash(
                        "ask",
                        "Ask the eBanking AI assistant a question")
                        .addOption(
                                OptionType.STRING,
                                "question",
                                "Your banking question",
                                true))
                .queue(
                        command -> System.out.println(
                                "Discord /ask command registered"),
                        error -> System.err.println(
                                "Discord command registration failed: "
                                        + error.getMessage()));

        System.out.println(
                "Discord bot connected: "
                        + jda.getSelfUser().getName());
    }

    @PreDestroy
    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
        }
    }
}
