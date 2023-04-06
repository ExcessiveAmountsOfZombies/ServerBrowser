package com.epherical.serverbrowser;

import com.epherical.serverbrowser.client.list.ServerBrowserList;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ServerQuery {


    private final String url;
    private final Consumer<URIBuilder> builderConsumer;
    private final Function<Throwable, String> exception;
    private final BiConsumer<String, Throwable> whenComplete;

    public ServerQuery(String url, Consumer<URIBuilder> builderConsumer, Function<Throwable, String> exception, BiConsumer<String, Throwable> whenComplete) {
        this.url = url;
        this.builderConsumer = builderConsumer;
        this.exception = exception;
        this.whenComplete = whenComplete;
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
            return "";
        }).exceptionally(exception);
    }

    public void buildList(CompletableFuture<String> future, ServerBrowserList list, boolean displayAtTop) {
        future.handle((s, throwable) -> {
           list.addEntries(JsonParser.parseString(s), displayAtTop);
           return s;
        }).whenComplete(whenComplete);
    }


}
