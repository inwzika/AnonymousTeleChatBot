package telegramBot;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler {

    private static Bot bot = Bot.getInstance();

    public static void handle(Message msg) {
        String userId = msg.getFrom().getId().toString();
        if (msg.isCommand()) {
            handleCommand(msg);
        } else if (bot.userState.get(userId) == State.START) {
            handleStartState(msg);
        } else if (bot.userState.get(userId) == State.REQUEST) {
            handleRequestState(msg);
        } else if (bot.userState.get(userId) == State.PENDING) {
            // todo
        } else if (bot.userState.get(userId) == State.CONNECT) {
            handleConnectState(msg);
        }
    }

    private static void handleCommand(Message msg) {
        User user = msg.getFrom();
        String userId = user.getId().toString();
        if (msg.getText().equals("/start")) {
            if (user.getUserName() != null) {
                bot.userIdMap.put(user.getUserName(), userId);
                bot.storeUser(user.getUserName(), userId);
                bot.userState.put(userId, State.START);
                ReplyKeyboard replyKeyboard = bot.getReplyKeyboardMarkup("Request to Connect");
                bot.sendMessage(userId, "Welcome to my Bot", replyKeyboard);
            }

        }
    }

    private static void handleStartState(Message msg) {
        String userId = msg.getFrom().getId().toString();
        if (msg.getText().equals("Request to Connect")) {
            bot.userState.put(userId, State.REQUEST);
            bot.sendMessage(userId, "Please enter a valid username starting with @");
        } else {
            ReplyKeyboard replyKeyboard = bot.getReplyKeyboardMarkup("Request to Connect");
            bot.sendMessage(userId, "please enter a valid command", replyKeyboard);
        }
    }

    private static void handleConnectState(Message msg) {
        String userId = msg.getFrom().getId().toString();
        if (bot.twoWayUsers.containsKey(userId)) {
            String userId2 = bot.twoWayUsers.get(userId);
            if (msg.getText().equals("End Chat")) {
                bot.twoWayUsers.remove(userId);
                bot.twoWayUsers.remove(userId2);
                bot.userState.put(userId, State.START);
                bot.userState.put(userId2, State.START);
                ReplyKeyboard replyKeyboard = bot.getReplyKeyboardMarkup("Request to Connect");
                bot.sendMessage(userId, "Chat ended", replyKeyboard);
                bot.sendMessage(userId2, "Chat ended", replyKeyboard);
            } else {
                bot.sendMessage(userId2, msg.getText());
            }
        }
    }

    private static void handleRequestState(Message msg) {
        String userId = msg.getFrom().getId().toString();
        char isValid = msg.getText().charAt(0);
        if (isValid == '@') {
            String userId2 = bot.userIdMap.get(msg.getText().substring(1));

            if (!bot.userIdMap.containsKey(msg.getText().substring(1))) {
                bot.sendMessage(userId, "This user is not joint to this Bot");
            } else {

                bot.pendingReqUsers.put(userId2, userId);

                bot.sendMessage(userId, "Your connection request sent. Please wait until the other accept your request.");
                ReplyKeyboard replyKeyboard = getInlineKeyboardMarkup();
                bot.sendMessage(userId2, "Someone requested to connect to you ", replyKeyboard);

                bot.userState.put(userId, State.PENDING);

            }
        } else {

            bot.sendMessage(userId, "Please enter a valid username starting with @");

        }
    }

    private static InlineKeyboardMarkup getInlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowInline = new ArrayList<>();

        List<InlineKeyboardButton> list = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton("Accept");
        InlineKeyboardButton button2 = new InlineKeyboardButton("Reject");
        button.setCallbackData("accept");
        button2.setCallbackData("reject");

        list.add(button);
        list.add(button2);

        rowInline.add(list);
        inlineKeyboardMarkup.setKeyboard(rowInline);
        return inlineKeyboardMarkup;

    }


}
