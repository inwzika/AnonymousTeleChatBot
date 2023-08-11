package telegramBot;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class CallbackHandler {

    public static void handle(CallbackQuery callbackQuery){

        Bot bot = Bot.getInstance();
        String data = callbackQuery.getData();
        if (data.equals("accept")){
            acceptRequest(callbackQuery, bot);

        }
        else if (data.equals("reject")){
            rejectRequest(callbackQuery, bot);
        }
    }

    private static void rejectRequest(CallbackQuery callbackQuery, Bot bot) {
        String userId2 = callbackQuery.getFrom().getId().toString();
        String userId = bot.pendingReqUsers.get(userId2);
        bot.sendMessage(userId , "Your request has been rejected");
        bot.pendingReqUsers.remove(userId2);
    }

    private static void acceptRequest(CallbackQuery callbackQuery, Bot bot) {
        String receiverId = callbackQuery.getFrom().getId().toString();
        String senderId = bot.pendingReqUsers.get(receiverId);
        bot.twoWayUsers.put(senderId, receiverId);
        bot.twoWayUsers.put(receiverId, senderId);
        ReplyKeyboard replyKeyboard = bot.getReplyKeyboardMarkup("End Chat");
        bot.sendMessage(senderId , "Successfully connected to @" + callbackQuery.getFrom().getUserName(), replyKeyboard);
        bot.sendMessage(receiverId, "Successfully connected", replyKeyboard);
        bot.pendingReqUsers.remove(receiverId);
        bot.userState.put(receiverId , State.CONNECT);
        bot.userState.put(senderId , State.CONNECT);
    }

}
