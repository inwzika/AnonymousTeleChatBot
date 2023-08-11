import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private final HashMap<String, String> userIdMap = new HashMap<>(); //key = username , value= id

    private final HashMap<String, String> twoWayUsers = new HashMap<>();// key= id sender - value= Id receiver

    private final HashMap<String, State> userState = new HashMap<>(); // key = id  - value = state

    private final HashMap<String , String> pendingReqUsers = new HashMap<>(); //key = id receiver - value = id sender
    private final Logger logger = new Logger();
    private final Properties properties = new Properties();

    public Bot() {
        try {
            properties.load(new FileInputStream("data.properties"));
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
            Message msg = update.getMessage();
            User user = msg.getFrom();
            String userId = user.getId().toString();
            logMessage(msg);
            if (msg.isCommand()) {
                if (msg.getText().equals("/start")) {
                    if (user.getUserName() != null) {
                        userIdMap.put(user.getUserName(), userId);
                        properties.put(user.getUserName(), userId);
                        try {
                            properties.store(new FileOutputStream("data.properties"), null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        userState.put(userId, State.START);
                        ReplyKeyboard replyKeyboard = getReplyKeyboardMarkup("Request to Connect");
                        sendMessage(userId, "Welcome to my Bot", replyKeyboard);
                    }

                }
            }
            else if (userState.get(userId) == State.START) {
                if (msg.getText().equals("Request to Connect")) {
                    userState.put(userId, State.REQUEST);
                    sendMessage(userId, "Please enter a valid username starting with @");
                } else {
                    ReplyKeyboard replyKeyboard = getReplyKeyboardMarkup("Request to Connect");
                    sendMessage(userId, "please enter a valid command", replyKeyboard);
                }
            }
            else if (userState.get(userId) == State.REQUEST) {
                sendRequest(msg);
            }
            else if (userState.get(userId) == State.PENDING){
                // todo
            }
            else if (userState.get(userId) == State.CONNECT ){
                if (twoWayUsers.containsKey(userId)) {
                    String userId2 = twoWayUsers.get(userId);
                    if (msg.getText().equals("End Chat")){
                        twoWayUsers.remove(userId);
                        twoWayUsers.remove(userId2);
                        userState.put(userId , State.START);
                        userState.put(userId2, State.START);
                        ReplyKeyboard replyKeyboard = getReplyKeyboardMarkup("Request to Connect");
                        sendMessage(userId,"Chat ended",replyKeyboard);
                        sendMessage(userId2,"Chat ended",replyKeyboard);
                    } else {
                        sendMessage(userId2, msg.getText());
                    }
                }
            }
        }
        else if (update.hasCallbackQuery()){
            CallbackQuery query = update.getCallbackQuery();
            String data = query.getData();
            if (data.equals("accept")){
              String receiverId = query.getFrom().getId().toString();
              String senderId = pendingReqUsers.get(receiverId);
              twoWayUsers.put(senderId, receiverId);
              twoWayUsers.put(receiverId, senderId);
              ReplyKeyboard replyKeyboard = getReplyKeyboardMarkup("End Chat");
              sendMessage(senderId , "Successfully connected to @" + query.getFrom().getUserName(), replyKeyboard);
              sendMessage(receiverId, "Successfully connected", replyKeyboard);
              pendingReqUsers.remove(receiverId);
              userState.put(receiverId ,State.CONNECT);
              userState.put(senderId ,State.CONNECT);

            }
            else if (data.equals("reject")){
                String userId2 = query.getFrom().getId().toString();
                String userId = pendingReqUsers.get(userId2);
                sendMessage(userId , "Your request has been rejected");
                pendingReqUsers.remove(userId2);
            }
        }
    }

    private void logMessage(Message message) {
        String s = "Id: " + message.getFrom().getId() + " - Name: " + message.getFrom().getFirstName() + " - Text: " + message.getText();
        System.out.println(s);
        logger.log(s);
    }

    public void sendMessage(String who, String what , ReplyKeyboard replyKeyboard) {
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
    public void sendMessage(String who , String what){
        sendMessage(who , what , null);
    }

    private ReplyKeyboardMarkup getReplyKeyboardMarkup(String... buttons) {
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

    private InlineKeyboardMarkup getInlineKeyboardMarkup(){
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowInline = new ArrayList<>();

        List <InlineKeyboardButton> list = new ArrayList<>();

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


    private void sendRequest(Message msg) {
        String userId = msg.getFrom().getId().toString();
        char isValid = msg.getText().charAt(0);
        if (isValid == '@') {
            String userId2 = userIdMap.get(msg.getText().substring(1));

            if (!userIdMap.containsKey(msg.getText().substring(1))){
                sendMessage(userId , "This user is not joint to this Bot");
            }
            else {

                pendingReqUsers.put(userId2 , userId);

                sendMessage(userId , "Your connection request sent. Please wait until the other accept your request.");
                ReplyKeyboard replyKeyboard = getInlineKeyboardMarkup();
                sendMessage(userId2 , "Someone requested to connect to you " , replyKeyboard);

                userState.put(userId , State.PENDING);

            }
        } else {

                sendMessage(userId, "Please enter a valid username starting with @");

        }
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
