package com.epherical.serverbrowser;

import com.google.gson.JsonParser;
import com.mojang.datafixers.types.Func;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServerQuery {


    private final String url;
    private final Consumer<URIBuilder> builderConsumer;
    private final Function<Throwable, Void> exception;

    public ServerQuery(String url, Consumer<URIBuilder> builderConsumer, Function<Throwable, Void> exception) {
        this.url = url;
        this.builderConsumer = builderConsumer;
        this.exception = exception;
    }


    public CompletableFuture<String> runQuery() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URIBuilder builder = new URIBuilder(url);
                builderConsumer.accept(builder);
                URL url = builder.build().toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    byte[] bytes = connection.getInputStream().readAllBytes();
                    return new String(bytes);
                }
                connection.disconnect();
            } catch (URISyntaxException | IOException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(exception);
    }


}
