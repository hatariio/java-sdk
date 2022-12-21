package io.hatari.client.java.io;

import io.hatari.client.java.HatariClient;
import io.hatari.client.java.util.GeneralUtil;
import io.hatari.client.java.util.HatariConstants;
import io.hatari.client.java.util.HatariLogging;
import io.hatari.client.java.util.UploadEventCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HatariHttpRequestRunnable implements Runnable {

    private final HatariClient hatariClient;
    private final String projectKey;
    private final Map<String, Object> event;
    private final UploadEventCallback callback;

    public HatariHttpRequestRunnable(HatariClient hatariClient, String projectKey, Map<String, Object> event, UploadEventCallback callback) {
        this.hatariClient = hatariClient;
        this.projectKey = projectKey;
        this.event = event;
        this.callback = callback;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection connection = sendEvent(this.projectKey, this.event);
            handleResult(connection.getInputStream(), connection.getResponseCode(), callback);
        } catch (IOException e) {
            HatariLogging.log("There was an error while sending events to the Keen API.");
            String stackTrace = GeneralUtil.getStackTraceFromThrowable(e);
            HatariLogging.log(stackTrace);
            if (callback != null) {
                callback.onError(stackTrace);
            }
        }
    }

    HttpURLConnection sendEvent(String eventCollection, Map<String, Object> event) throws IOException {
        // just using basic JDK HTTP library
        String urlString = String.format("%s/%s/projects/%s/events/%s", HatariConstants.SERVER_ADDRESS,
                HatariConstants.API_VERSION, hatariClient.getProjectKey(), eventCollection);
        URL url = new URL(urlString);

        // set up the POST
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", hatariClient.getApiKey());
        connection.setRequestProperty("Content-Type", "application/json");
        // we're writing
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();
        // write JSON to the output stream
        HatariClient.MAPPER.writeValue(out, event);
        out.close();
        return connection;
    }

    static void handleResult(InputStream input, int responseCode, UploadEventCallback callback) {
        if (responseCode == 201) {
            // event add worked
            if (callback != null) {
                // let the caller know if they've registered a callback
                callback.onSuccess();
            }
        } else {
            // if the response was bad, make a note of it
            HatariLogging.log(String.format("Response code was NOT 201. It was: %d", responseCode));
            String responseBody = GeneralUtil.convertStreamToString(input);
            HatariLogging.log(String.format("Response body was: %s", responseBody));
            if (callback != null) {
                // let the caller know if they've registered a callback
                callback.onError(responseBody);
            }
        }
    }
}