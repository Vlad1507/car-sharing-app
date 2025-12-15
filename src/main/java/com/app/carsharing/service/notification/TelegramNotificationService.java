package com.app.carsharing.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class TelegramNotificationService implements NotificationService {
    private final String adminChatId;
    private final DefaultAbsSender telegramSender;

    public TelegramNotificationService(@Value("${telegram.bot.token}") String token,
                                       @Value("${telegram.admin.chat-id}") String adminChatId) {
        this.adminChatId = adminChatId;
        DefaultBotOptions botOptions = new DefaultBotOptions();
        this.telegramSender = new DefaultAbsSender(botOptions, token) {
            @Override
            public String getBotToken() {
                return super.getBotToken();
            }
        };
    }

    @Override
    public void sendNotification(String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(adminChatId)
                .text(message)
                .build();
        try {
            telegramSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram notification to {}: {}, ",
                    adminChatId,
                    e.getMessage());
        }
    }
}
