package com.epherical.serverbrowser.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;

public class ServerBrowserFabClient implements ClientModInitializer {

    public static JsonElement servers;

    @Override
    public void onInitializeClient() {
        try {
            URIBuilder builder = new URIBuilder("http://localhost:8080/api/v1/servers");
            builder.addParameter("type", "Survival");
            builder.addParameter("type", "AOF5");
            URL url = builder.build().toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                byte[] bytes = connection.getInputStream().readAllBytes();
                String string = new String(bytes);
                servers = JsonParser.parseString(string);
            }
            connection.disconnect();
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }


    }
}
