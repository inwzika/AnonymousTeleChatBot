import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;

public class Bot extends TelegramLongPollingBot {

    private final HashMap<String, String> userIdMap = new HashMap<>(); //key = username , value= id

    private final HashMap<String, String> twoWayUsers = new HashMap<>();// key= id sender - value= Id receiver


    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        User user = msg.getFrom();
        String userId = user.getId().toString();
        if (msg.isCommand()) {
            if (msg.getText().equals("/start")) {
                sendText(userId, "Welcome to my Bot");
                if (user.getUserName() != null) {
                    userIdMap.put(user.getUserName(), userId);
                }
            }
        } else {
            char isValid = msg.getText().charAt(0);
            if (isValid == '@') {
                String userId2 = userIdMap.get(msg.getText().substring(1));
                twoWayUsers.put(userId, userId2);
                twoWayUsers.put(userId2, userId);
            } else {
                if (twoWayUsers.containsKey(userId)) {
                    String userId2 = twoWayUsers.get(userId);
                    sendText(userId2, msg.getText());
                } else {
                    sendText(userId, "Please enter a valid username starting with @");
                }
            }
        }
        logMessage(msg, true);
    }


    private void logMessage(Message message) {
        System.out.println("Name: " + message.getFrom().getFirstName() + " - Text: " + message.getText());
    }

    private void logMessage(Message message, boolean printId) {
        if (printId) {
            System.out.println("Id: " + message.getFrom().getId() + " - Name: " + message.getFrom().getFirstName() + " - Text: " + message.getText());
        } else
            logMessage(message);
    }

    public void sendText(String who, String what) {
//        SendMessage sm = new SendMessage(who.toString(),what);
        SendMessage sm = SendMessage.builder()
                .chatId(who) //Who are we sending a message to
                .text(what).build();    //Message content
        try {
            execute(sm);                        //Actually sending the message
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);      //Any error will be printed here
        }
    }

    @Override
    public String getBotUsername() {
        return "anony_chat_bot";
    }

    @Override
    public String getBotToken() {
        return Main.token;
    }
}
