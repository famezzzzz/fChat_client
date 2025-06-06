package ru.top;

import org.json.JSONObject;
import org.json.JSONArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ChatClient {
    private static final String BASE_URL = "http://localhost:38080";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static String senderJwtToken = null;
    private static String receiverJwtToken = null;
    private static String senderId = null;
    private static String receiverId = null;
    private static String groupId = null;
    private static int msgCount = 1;
    private static String lastPrivateMsg = null;


    public static void main(String[] args) {



        while (true){
            try {
                Scanner scanner = new Scanner(System.in);
                System.out.println("POST register users\n" +
                        "2. POST login\n" +
                        "3. GET user details\n" +
                        "4. POST create group\n" +
                        "5. POST group message by sender\n" +
                        "6. GET group messages\n" +
                        "7. POST private message to receiver\n" +
                        "8. GET private messages by receiver\n" +
                        "9. POST private message to sender\n" +
                        "10. GET private messages by sender\n" +
                        "11. GET chat history\n" +
                        "12. GET search messages\n" +
                        "13. GET count users" +
                        "14. GET current user info");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        // Register a user
                        System.out.println("Registering user...");
                        senderId = registerUser("Sender", "password", "11-06-1987", "sender@example.com", "+1234567890", "https://example.com/senderAvatar.jpg");
                        System.out.println("Sender registered with ID: " + senderId);

                        System.out.println("Registering user...");
                        receiverId = registerUser("Receiver", "qwerty", "04-12-1989", "receiver@example.com", "+0987654321", "https://example.com/senderAvatar.jpg");
                        System.out.println("Receiver registered with ID: " + receiverId);
                        break;

                    case 2:
                        // Login to get JWT
                        System.out.println("Sender logging in...");
                        senderJwtToken = login("Sender", "password");
                        System.out.println("JWT Token: " + senderJwtToken);

                        System.out.println("Receiver logging in...");
                        receiverJwtToken = login("Receiver", "qwerty");
                        System.out.println("JWT Token: " + receiverJwtToken);
                        break;

                    case 3:
                        // Get user details
                        System.out.println("Fetching receiver details...");
                        String userDetails = getUserDetails(receiverId, senderJwtToken);
                        System.out.println("User Details: " + userDetails);
                        break;

                    case 4:
                        // Create a group
                        System.out.println("Creating group...");
                        groupId = createGroup("TestGroup", senderJwtToken);
                        System.out.println("Group created with ID: " + groupId);
                        break;

                    case 5:
                        // Send a group message
                        System.out.println("Sending group message...");
                        sendGroupMessage(senderId, groupId, "Hello group!", senderJwtToken);
                        System.out.println("Group message sent.");
                        break;

                    case 6:
                        // Fetch group messages
                        System.out.println("Fetching group messages...");
                        String groupMessages = getGroupMessages(groupId, receiverJwtToken);
                        System.out.println("Group Messages: " + groupMessages);
                        break;

                    case 7:
                        // Send a private message
                        System.out.println("Sending private message to receiver...");
                        sendPrivateMessage(senderId, receiverId, "Hello, receiver, I`m a Sender and this is " + msgCount + " message!" , senderJwtToken);
                        msgCount++;
                        System.out.println("Private message sent.");
                        break;

                    case 8:
                        // Get private messages
                        System.out.println("Fetching private messages by receiver...");
                        lastPrivateMsg = getPrivateMessages(senderId, LocalDateTime.now().minusHours(24), receiverJwtToken);
                        System.out.println("Private Messages: " + lastPrivateMsg);
                        break;

                    case 9:
                        // Send a private message
                        System.out.println("Sending private message to sender...");
                        sendPrivateMessage(receiverId, senderId, "Hello, Sender, I`m a Receiver and this is " + msgCount + " message!", receiverJwtToken);
                        msgCount++;
                        System.out.println("Private message sent.");
                        break;

                    case 10:
                        // Get private messages
                        System.out.println("Fetching private messages by sender...");
                        lastPrivateMsg = getPrivateMessages(receiverId, LocalDateTime.now().minusHours(24), senderJwtToken);
                        System.out.println("Private Messages: " + lastPrivateMsg);
                        break;

                    case 11:
                        // Get chat history
                        System.out.println("Get chat history...");
                        String chatHistory = chatHistory(receiverId, senderJwtToken);
                        System.out.println("Private Messages: " + chatHistory);
                        break;

                    case 12:
                        // Search messages
                        System.out.println("Searching messages with keyword 'hello'...");
                        String searchResults = searchMessages("hello", LocalDateTime.now().minusDays(1), null, senderJwtToken);
                        System.out.println("Search Results: " + searchResults);

                    case 13:
                        // Count users
                        System.out.println("Count users...");
                        String userCount = countUsers(senderJwtToken);
                        System.out.println("Private Messages: " + userCount);
                        break;

                    case 14:
                        // Current user info
                        System.out.println("Fetching" + senderId + "details...");
                        String myDetails = getMyDetails(senderJwtToken);
                        System.out.println("User Details: " + myDetails);
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + choice);
                }





            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                if (e.getMessage().contains("SQLITE_ERROR")) {
                    System.err.println("Database error detected. Ensure the database is initialized with init.sql and includes the chat_user_groups table.");
                    System.err.println("Run: rm /Users/urijvazmin/chat.db && sqlite3 /Users/urijvazmin/chat.db < init.sql");
                }
                e.printStackTrace();
            }
        }
        }

    private static String registerUser(String username, String password, String birthdate, String email, String phone, String avatarUrl) throws Exception {
        JSONObject requestBody = new JSONObject()
                .put("username", username)
                .put("password", password)
                .put("birthdate", birthdate)
                .put("email", email)
                .put("phone", phone)
                .put("avatarUrl", avatarUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JSONObject responseJson = new JSONObject(response.body());
            return responseJson.getString("id");
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Registration failed: " + errorJson.getString("error"));
        }
    }

    private static String login(String username, String password) throws Exception {
        JSONObject requestBody = new JSONObject()
                .put("username", username)
                .put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JSONObject responseJson = new JSONObject(response.body());
            return responseJson.getString("token");
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Login failed: " + errorJson.getString("error"));
        }
    }

    private static String getUserDetails(String userId, String jwtToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/" + userId))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to fetch user details: " + errorJson.getString("error"));
        }
    }

    private static String getMyDetails(String jwtToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/myInfo"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to fetch user details: " + errorJson.getString("error"));
        }
    }

    private static String createGroup(String groupName, String jwtToken) throws Exception {
        JSONObject requestBody = new JSONObject()
                .put("name", groupName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/groups/create"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JSONObject responseJson = new JSONObject(response.body());
            return responseJson.getString("id");
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Group creation failed: " + errorJson.getString("error"));
        }
    }


    private static void sendPrivateMessage(String senderId, String recipientId, String content, String jwtToken) throws Exception {
        JSONObject requestBody = new JSONObject()
                .put("content", content)
                .put("senderId", senderId)
                .put("recipientId", recipientId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/messages/private"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to send private message: " + errorJson.getString("error"));
        }
    }

    private static void sendGroupMessage(String senderId, String groupId, String content, String jwtToken) throws Exception {
        JSONObject requestBody = new JSONObject()
                .put("content", content)
                .put("senderId", senderId)
                .put("groupId", groupId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/messages/group"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to send group message: " + errorJson.getString("error"));
        }
    }

    private static String getPrivateMessages(String otherUserId, LocalDateTime since, String jwtToken) throws Exception {
        String uri = BASE_URL + "/api/messages/private/conversation/" + otherUserId;
        if (since != null) {
            uri += "?since=" + since.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to fetch private messages: " + errorJson.getString("error"));
        }
    }

    private static String getGroupMessages(String groupId, String jwtToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/messages/group/" + groupId))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to fetch group messages: " + errorJson.getString("error"));
        }
    }

    private static String chatHistory(String senderId, String jwtToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/messages/private/history/" + receiverId))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to get chat history: " + errorJson.getString("error"));
        }
    }

    private static String countUsers(String jwtToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/users/count"))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to get chat history: " + errorJson.getString("error"));
        }
    }

    private static String searchMessages(String keyword, LocalDateTime start, LocalDateTime end, String jwtToken) throws Exception {
        StringBuilder uri = new StringBuilder(BASE_URL + "/api/messages/search");
        boolean firstParam = true;
        if (keyword != null && !keyword.isEmpty()) {
            uri.append(firstParam ? "?" : "&").append("keyword=").append(keyword);
            firstParam = false;
        }
        if (start != null) {
            uri.append(firstParam ? "?" : "&").append("start=").append(start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            firstParam = false;
        }
        if (end != null) {
            uri.append(firstParam ? "?" : "&").append("end=").append(end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri.toString()))
                .header("Authorization", "Bearer " + jwtToken)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        } else {
            JSONObject errorJson = new JSONObject(response.body());
            throw new Exception("Failed to search messages: " + errorJson.getString("error"));
        }
    }
}