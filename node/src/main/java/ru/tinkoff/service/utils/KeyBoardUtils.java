package ru.tinkoff.service.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyBoardUtils {
    public InlineKeyboardMarkup createInlineKeyboardButtons(int rows, int buttonInRows, String[] buttonsText, String[] callbackData) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        for (int i = 1; i <= rows; i++) {
            List<InlineKeyboardButton> rowInLine = new ArrayList<>();
            for (int j = 1; j <= buttonInRows; j++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(buttonsText[i * j - 1]);
                button.setCallbackData(callbackData[i * j - 1]);
                rowInLine.add(button);
            }
            rowsInLine.add(rowInLine);
        }
        inlineKeyboardMarkup.setKeyboard(rowsInLine);
        return inlineKeyboardMarkup;
    }
    
    public ReplyKeyboardMarkup createReplyKeyboardMarkup(int buttonInRows, String[] buttonsText) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        for (int i = 0; i < buttonInRows; i++) {
            row.add(buttonsText[i]);
        }
        keyboardRows.add(row);
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }
}
