import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

public class SendDirectMessage {
    private static String token = "YOUR_TOKEN";

    public static void main(String[] args) {
        token = args[0];

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Please enter your chatId : ");
            String chatId = scanner.next();
            System.out.println("Please enter your text : ");
            scanner.nextLine();
            String message = scanner.nextLine();


            sendMessage(chatId, message);
        }

    }
    public static boolean sendMessage(String chatId , String message){
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .version(HttpClient.Version.HTTP_2)
                .build();

        UriBuilder builder = UriBuilder
                .fromUri("https://api.telegram.org")
                .path("/{token}/sendMessage")
                .queryParam("chat_id", chatId)
                .queryParam("text", message);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(builder.build("bot" + token))
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = client
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200){
                System.out.println("Message Sent successfully.");
                return true;
            }
            else {
                System.out.println("Message not sent");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Message not sent");
            return false;
        }
    }
}
