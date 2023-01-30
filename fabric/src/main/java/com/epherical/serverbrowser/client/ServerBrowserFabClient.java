package com.epherical.serverbrowser.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerBrowserFabClient implements ClientModInitializer {

    public static JsonElement servers;

    @Override
    public void onInitializeClient() {
        try {
            URL url = new URL("http://localhost:8080/api/v1/servers?type=Survival");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                byte[] bytes = connection.getInputStream().readAllBytes();
                String string = new String(bytes);
                servers = JsonParser.parseString(string);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
