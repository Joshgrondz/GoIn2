import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONObject;

public class LocationFetcher implements Runnable {
    private EventGeofenceController geofenceController;
    private static final String BASE_URL = "https://webapplication120250408230542-draxa5ckg5gabacc.canadacentral-01.azurewebsites.net";
    HttpClient client;



    //EventGeofenceController controller
    public LocationFetcher() {
        //this.geofenceController = controller;
        client = HttpClient.newHttpClient();
    }

    @Override
    public void run() {


        //See if i can call
        String endpoint1 = "/api/User";
        try {
            makeGetRequest(client, endpoint1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //get location data of chaperone
        //Update Chaperone


        //get location data of Students
        //parse into UserList
        //Update Students locations



    }

    private static void makeGetRequest(HttpClient client, String endpoint) throws Exception {
        URI requestUri = URI.create(BASE_URL + endpoint); // Combine base URL and endpoint

        HttpRequest request = HttpRequest.newBuilder()
                .uri(requestUri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("\nGET Request to: " + requestUri);
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body:\n" + response.body());

        // Parse JSON (example)
        if (response.statusCode() == 200) {
            if (response.body().startsWith("[")) { // Check if it's a JSON array
                JSONArray jsonArray = new JSONArray(response.body());
                System.out.println("\nFirst item in array: " + jsonArray.getJSONObject(0).toString());
            } else if (response.body().startsWith("{")) { // Check if it's a JSON object
                JSONObject jsonObject = new JSONObject(response.body());
                System.out.println("\nTitle: " + jsonObject.getString("title"));
            }
        }
    }


}