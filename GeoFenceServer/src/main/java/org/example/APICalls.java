package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.lang.InterruptedException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class APICalls {

    private static String BASE_URL = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net";
    private static final String NOTIFICATION_ENDPOINT = "/api/Notification";
    private static final String LOG_ENDPOINT = "/api/Log";

    public static JSONObject makeGetRequestSingleItem(HttpClient client, String endpoint) throws IOException, InterruptedException, JSONException {
        URI requestUri = URI.create(BASE_URL + endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject jsonObject = null;
        if (response.statusCode() == 200) {
            try {
                jsonObject = new JSONObject(response.body());
            } catch (JSONException e) {
                System.err.println("Error parsing JSON response from GET " + requestUri + ": " + e.getMessage());
                System.err.println("Response body: " + response.body());
                throw e;
            }
        } else {
            System.err.println("GET request to " + requestUri + " failed with status code: " + response.statusCode());
        }

        return jsonObject;
    }

    public static JSONArray makeGetRequestMultiItem(HttpClient client, String endpoint) throws IOException, InterruptedException, JSONException {
        URI requestUri = URI.create(BASE_URL + endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONArray jsonArray = null;
        if (response.statusCode() == 200) {
            try {
                jsonArray = new JSONArray(response.body());
            } catch (JSONException e) {
                System.err.println("Error parsing JSON response from GET " + requestUri + ": " + e.getMessage());
                System.err.println("Response body: " + response.body());
                throw e;
            }
        } else {
            System.err.println("GET request to " + requestUri + " failed with status code: " + response.statusCode());
        }

        return jsonArray;
    }

    public static HttpResponse<String> makePostRequest(HttpClient client, String endpoint, String jsonData) throws IOException, InterruptedException {
        URI requestUri = URI.create(BASE_URL + endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .POST(HttpRequest.BodyPublishers.ofString(jsonData))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            System.out.println("POST request to " + requestUri + " successful (Status: " + response.statusCode() + ")");
        } else {
            System.err.println("POST request to " + requestUri + " failed with status code: " + response.statusCode() + "\nResponse Body: " + response.body());
        }

        return response;
    }

    public static HttpResponse<String> makePostRequest(HttpClient client, String endpoint, JSONObject jsonPayload) throws IOException, InterruptedException {
        String payloadString;
        try {
            payloadString = jsonPayload.toString();
        } catch (JSONException e) {
            System.err.println("Internal Error: Failed to convert JSONObject to String: " + e.getMessage());
            throw new IOException("Failed to serialize JSON payload", e);
        }
        return makePostRequest(client, endpoint, payloadString);
    }

    public static void sendNotification(User recipient, int eventID, String message, HttpClient client) throws Exception {
        try {
            JSONObject notificationPayload = new JSONObject();
            JSONObject logPayload = new JSONObject();

            notificationPayload.put("userid", recipient.getID());
            notificationPayload.put("eventid", eventID);
            notificationPayload.put("notificationDescription", message);

            logPayload.put("eventID", eventID);
            logPayload.put("logDescription", "Sent notification - \"" + message + "\" - to user: " + recipient.getFirstName() + " " + recipient.getLastName() + " (ID: " + recipient.getID() + ")");


            Instant now = Instant.now();
            String timestampString = DateTimeFormatter.ISO_INSTANT.format(now);
            notificationPayload.put("notificationTimestamp", timestampString);
            logPayload.put("timestamp", timestampString);


            notificationPayload.put("sent", true);

            System.out.println("Sending notification: " + notificationPayload.toString(2));
            HttpResponse<String> notificationResponse = makePostRequest(client, NOTIFICATION_ENDPOINT, notificationPayload);

            System.out.println("Sending log: " + logPayload.toString(2));
            HttpResponse<String> logResponse = makePostRequest(client, LOG_ENDPOINT, logPayload);

        } catch (JSONException e) {
            System.err.println("Error creating notification/log JSON payload: " + e.getMessage());
            throw new Exception("Failed to create notification/log JSON", e);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error sending notification/log POST request: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new Exception("Failed to send notification/log", e);
        }
        catch (Exception e) {
            System.err.println("An unexpected error occurred during sendNotification: " + e.getMessage());
            throw e;
        }
    }
}