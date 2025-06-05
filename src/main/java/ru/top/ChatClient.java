package ru.top;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private static String jwtToken = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            // Step 1: Register a user
            System.out.println("Registering user...");
            String userId = registerUser("testuser", "password123", "01-01-1990", "test@example.com", "+1234567890", "https://example.com/avatar.jpg");
            System.out.println("User registered with ID: " + userId);

            // Step 2: Login to get JWT
            System.out.println("Logging in...");
            jwtToken = login("testuser", "password123");
            System.out.println("JWT Token: " + jwtToken);

            // Step 3: Get user details
            System.out.println("Fetching user details...");
            String userDetails = getUserDetails(userId);
            System.out.println("User Details: " + userDetails);

            // Step 4: Create a group
            System.out.println("Creating group...");
            String groupId = createGroup("TestGroup");
            System.out.println("Group created with ID: " + groupId);

            // Step 5: Send a private message
            System.out.println("Sending private message...");
            sendPrivateMessage(userId, userId, "Hello, this is a test message!");
            System.out.println("Private message sent.");

            // Step 6: Send a group message
            System.out.println("Sending group message...");
            sendGroupMessage(userId, groupId, "Hello group!");
            System.out.println("Group message sent.");

            // Step 7: Fetch private conversation messages
            System.out.println("Fetching private messages...");
            String privateMessages = getPrivateMessages(userId, LocalDateTime.now().minusHours(24));
            System.out.println("Private Messages: " + privateMessages);

            // Step 8: Fetch group messages
            System.out.println("Fetching group messages...");
            String groupMessages = getGroupMessages(groupId);
            System.out.println("Group Messages: " + groupMessages);

            // Step 9: Search messages
            System.out.println("Searching messages with keyword 'hello'...");
            String searchResults = searchMessages("hello", LocalDateTime.now().minusDays(1), null);
            System.out.println("Search Results: " + searchResults);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
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

    private static String getUserDetails(String userId) throws Exception {
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

    private static String createGroup(String groupName) throws Exception {
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

    private static void sendPrivateMessage(String senderId, String recipientId, String content) throws Exception {
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

    private static void sendGroupMessage(String senderId, String groupId, String content) throws Exception {
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

    private static String getPrivateMessages(String otherUserId, LocalDateTime since) throws Exception {
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

    private static String getGroupMessages(String groupId) throws Exception {
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

    private static String searchMessages(String keyword, LocalDateTime start, LocalDateTime end) throws Exception {
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