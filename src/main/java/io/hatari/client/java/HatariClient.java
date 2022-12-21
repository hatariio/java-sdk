package io.hatari.client.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.hatari.client.java.exceptions.InvalidEventException;
import io.hatari.client.java.exceptions.InvalidEventException;
import io.hatari.client.java.exceptions.HatariException;
import io.hatari.client.java.util.HatariConfig;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * HatariClient has static methods to return managed instances of itself and instance methods to collect new events
 * and upload them through the Hatari API.
 * <p/>
 * Example usage:
 * <p/>
 * <pre>
 *     HatariClient.initialize("project_token");
 *     Map<String, Object> myEvent = new HashMap<String, Object>();
 *     myEvent.put("property name", "property value");
 *     HatariClient.client().addEvent("transactions", myEvent);
 *     HatariClient.client().upload(null);
 * </pre>
 *
 * @author Ebot Tabi
 * @since 1.0.0
 */
public class HatariClient {

    public static final ObjectMapper MAPPER;
    public static final ExecutorService EXECUTOR_SERVICE;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        EXECUTOR_SERVICE = Executors.newFixedThreadPool(HatariConfig.NUM_THREADS_FOR_HTTP_REQUESTS);
    }

    private final String projectKey;
    private final String apiKey;
    //private GlobalPropertiesEvaluator globalPropertiesEvaluator;
    private Map<String, Object> globalProperties;

    private enum ClientSingleton {
        INSTANCE;
        private HatariClient client;
    }

    public static void initialize(String projectId, String apiKey) {
        ClientSingleton.INSTANCE.client = new HatariClient(projectId, apiKey);
    }

    public static HatariClient client() {
        if (ClientSingleton.INSTANCE.client == null) {
            throw new IllegalStateException("Please call HatariClient.initialize() before requesting the shared client.");
        }
        return ClientSingleton.INSTANCE.client;
    }

    /**
     * Getter for the Hatari Project KEY associated with this instance of the {@link HatariClient}.
     *
     * @return the Hatari Project KEY
     */
    public String getProjectKey() {
        return projectKey;
    }

    /**
     * Getter for the Hatari API Key associated with this instance of the {@link HatariClient}.
     *
     * @return the Hatari API Key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Call this if your code needs to use more than one Hatari project and API Key (or if you don't want to use
     * the managed, singleton instance provided by this library).
     *
     * @param projectKey The ID of your project.
     * @param apiKey    The API Key for your project.
     */
    public HatariClient(String projectKey, String apiKey) {
        if (projectKey == null || projectKey.length() == 0) {
            throw new IllegalArgumentException("Invalid project ID specified: " + projectKey);
        }
        if (apiKey == null || apiKey.length() == 0) {
            throw new IllegalArgumentException("Invalid API Key specified: " + apiKey);
        }

        this.projectKey = projectKey;
        this.apiKey = apiKey;
        //this.globalPropertiesEvaluator = null;
        this.globalProperties = null;
    }

}
