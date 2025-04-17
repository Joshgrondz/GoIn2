package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class APICalls {
    private static String BASE_URL = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net";

    //Single item get
    public static JSONObject makeGetRequestSingleItem(HttpClient client, String endpoint) throws Exception {
        URI requestUri = URI.create(BASE_URL + endpoint); // Combine base URL and endpoint

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        /* Printing for testing to see what is retrieved
        System.out.println("\nGET Request to: " + requestUri);
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body:\n" + response.body());
         */

        JSONObject jsonObject = null;
        // Parse JSON (example)
        if (response.statusCode() == 200) {
           jsonObject = new JSONObject(response.body());
        }

        return jsonObject;

    }

    //Multi item Get
    public static JSONArray makeGetRequestMultiItem(HttpClient client, String endpoint) throws Exception {
        URI requestUri = URI.create(BASE_URL + endpoint); // Combine base URL and endpoint

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        /* Printing for testing to see what is retrieved
        System.out.println("\nGET Request to: " + requestUri);
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body:\n" + response.body());
         */

        JSONArray jsonArray = null;
        // Parse JSON (example)
        if (response.statusCode() == 200) {
            jsonArray = new JSONArray(response.body());
        }

        return jsonArray;

    }
}
