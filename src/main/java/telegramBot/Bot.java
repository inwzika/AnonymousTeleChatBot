package telegramBot;

import conf.Config;
import logger.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private static Bot instance = new Bot();

    public static Bot getInstance() {
        return instance;
    }


    final HashMap<String, String> userIdMap = new HashMap<>(); //key = username , value= id
    final HashMap<String, String> twoWayUsers = new HashMap<>();// key= id sender - value= Id receiver
    final HashMap<String, State> userState = new HashMap<>(); // key = id  - value = state
    final HashMap<String, String> pendingReqUsers = new HashMap<>(); //key = id receiver - value = id sender
    private final Logger logger = new Logger();
    private final Properties properties = new Properties();

    private Bot() {
        try {
            properties.load(new FileInputStream("resources/data.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String key : properties.stringPropertyNames()) {
            userIdMap.put(key, properties.get(key).toString());
            userState.put(properties.get(key).toString(), State.START);
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            logMessage(update.getMessage());
            MessageHandler.handle(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            CallbackHandler.handle(update.getCallbackQuery());
        }
    }


    void sendMessage(String who, String what, ReplyKeyboard replyKeyboard) {
        SendMessage sm = SendMessage.builder()
                .chatId(who) //Who are we sending a message to
                .text(what).build();    //Message content
        sm.setReplyMarkup(replyKeyboard);
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            e.printStackTrace();     //Any error will be printed here
        }
    }

    void sendMessage(String who, String what) {
        sendMessage(who, what, null);
    }

    ReplyKeyboardMarkup getReplyKeyboardMarkup(String... buttons) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.addAll(Arrays.asList(buttons));

        rows.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    void storeUser(String username, String userId) {
        properties.put(username, userId);
        try {
            properties.store(new FileOutputStream("resources/data.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void logMessage(Message message) {
        String s = "Id: " + message.getFrom().getId() + " - Name: " + message.getFrom().getFirstName() + " - Text: " + message.getText();
        System.out.println(s);
        logger.log(s);
    }

    @Override
    public String getBotUsername() {
        return Config.username;
    }

    @Override
    public String getBotToken() {
        return Config.token;
    }

}
