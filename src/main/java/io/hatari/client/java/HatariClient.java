package io.hatari.client.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.hatari.client.java.data.GlobalPropertiesEvaluator;
import io.hatari.client.java.exceptions.InvalidEventException;
import io.hatari.client.java.exceptions.InvalidEventException;
import io.hatari.client.java.exceptions.HatariException;
import io.hatari.client.java.exceptions.InvalidProjectException;
import io.hatari.client.java.io.HatariHttpRequestRunnable;
import io.hatari.client.java.util.HatariConfig;
import io.hatari.client.java.util.HatariLogging;
import io.hatari.client.java.util.UploadEventCallback;

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
    private GlobalPropertiesEvaluator globalPropertiesEvaluator;
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
        this.globalPropertiesEvaluator = null;
        this.globalProperties = null;
    }

    /**
     * Getter for the {@link GlobalPropertiesEvaluator} associated with this instance of the {@link HatariClient}.
     *
     * @return the {@link GlobalPropertiesEvaluator}
     */
    public GlobalPropertiesEvaluator getGlobalPropertiesEvaluator() {
        return globalPropertiesEvaluator;
    }

    /**
     * Call this any time you want to add an event that will eventually be sent to the Hatari IO server.
     * <p/>
     * The event will be stored on the local file system until you decide to upload (usually this will happen
     * right before your app goes into the background, but it could be any time).
     *
     * @param event           A Map that consists of key/value pairs. Hatari naming conventions apply (see docs).
     *                        Nested Maps and lists are acceptable (and encouraged!).
     * @throws HatariException
     */
    public void addEvent(String projectKey, Map<String, Object> event) throws HatariException {
        addEvent(projectKey, event, null, null);
    }

    /**
     * Call this any time you want to add an event that will eventually be sent to the Hatari IO server AND
     * you want to override Hatari-defaulted properties (like timestamp).
     * <p/>
     * The event will be stored on the local file system until you decide to upload (usually this will happen
     * right before your app goes into the background, but it could be any time).
     *
     * @param projectKey The project key you want to put this event into.
     * @param event           A Map that consists of key/value pairs. Keen naming conventions apply (see docs).
     *                        Nested Maps and lists are acceptable (and encouraged!).
     * @param hatariProperties  A Map that consists of key/value pairs to override default properties.
     *                        ex: "timestamp" -> Calendar.getInstance()
     * @param callback        An instance of AddEventCallback. Will invoke onSuccess when adding the event succeeds.
     *                        Will invoke onError when adding the event fails.
     * @throws HatariException
     */
    public void addEvent(String projectKey, Map<String, Object> event, Map<String, Object> hatariProperties,
                         UploadEventCallback callback) throws HatariException {
        // get the event
        Map<String, Object> newEvent = validateAndBuildEvent(projectKey, event, hatariProperties);
        // send the request as a callable in another thread
        EXECUTOR_SERVICE.submit(new HatariHttpRequestRunnable(this, projectKey, newEvent, callback));
    }

    Map<String, Object> validateAndBuildEvent(String projectKey, Map<String, Object> event,
                                              Map<String, Object> hatariProperties) throws HatariException {
        validateEventCollection(projectKey);
        validateEvent(event);

        HatariLogging.log(String.format("Adding event to project: %s", projectKey));

        // build the event
        Map<String, Object> newEvent = new HashMap<String, Object>();
        // handle hatari properties
        Calendar timestamp = Calendar.getInstance();
        if (hatariProperties == null) {
            hatariProperties = new HashMap<String, Object>();
            hatariProperties.put("timestamp", timestamp);
        } else {
            if (!hatariProperties.containsKey("timestamp")) {
                hatariProperties.put("timestamp", timestamp);
            }
        }
        newEvent.put("hatari", hatariProperties);

        // handle global properties
        Map<String, Object> globalProperties = getGlobalProperties();
        if (globalProperties != null) {
            newEvent.putAll(globalProperties);
        }
        GlobalPropertiesEvaluator globalPropertiesEvaluator = getGlobalPropertiesEvaluator();
        if (globalPropertiesEvaluator != null) {
            Map<String, Object> props = globalPropertiesEvaluator.getGlobalProperties(projectKey);
            if (props != null) {
                newEvent.putAll(props);
            }
        }
        // now handle user-defined properties
        newEvent.putAll(event);

        return newEvent;
    }

    private void validateEventCollection(String projectKey) throws InvalidProjectException {
        if (projectKey == null || projectKey.length() == 0) {
            throw new InvalidProjectException("You must specify a non-null, non-empty event collection: " + projectKey);
        }
        if (projectKey.startsWith("$")) {
            throw new InvalidProjectException("An event collection name cannot start with the dollar sign ($) character.");
        }
        if (projectKey.length() > 256) {
            throw new InvalidProjectException("An event collection name cannot be longer than 256 characters.");
        }
    }

    private void validateEvent(Map<String, Object> event) throws InvalidEventException {
        validateEvent(event, 0);
    }


    @SuppressWarnings("unchecked") // cast to generic Map will always be okay in this case
    private void validateEvent(Map<String, Object> event, int depth) throws InvalidEventException {
        if (depth == 0) {
            if (event == null || event.size() == 0) {
                throw new InvalidEventException("You must specify a non-null, non-empty event.");
            }
            if (event.containsKey("hatari")) {
                throw new InvalidEventException("An event cannot contain a root-level property named 'hatari'.");
            }
        }

        for (Map.Entry<String, Object> entry : event.entrySet()) {
            String key = entry.getKey();
            if (key.contains(".")) {
                throw new InvalidEventException("An event cannot contain a property with the period (.) character in it.");
            }
            if (key.startsWith("$")) {
                throw new InvalidEventException("An event cannot contain a property that starts with the dollar sign ($) character in it.");
            }
            if (key.length() > 256) {
                throw new InvalidEventException("An event cannot contain a property name longer than 256 characters.");
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                String strValue = (String) value;
                if (strValue.length() >= 10000) {
                    throw new InvalidEventException("An event cannot contain a string property value longer than 10,000 characters.");
                }
            } else if (value instanceof Map) {
                validateEvent((Map<String, Object>) value, depth + 1);
            }
        }
    }


    /**
     * Call this to set the {@link GlobalPropertiesEvaluator} for this instance of the {@link HatariClient}.
     * The evaluator is invoked every time an event is added to an event collection.
     * <p/>
     * Global properties are properties which are sent with EVERY event. For example, you may wish to always
     * capture device information like OS version, handset type, orientation, etc.
     * <p/>
     * The evaluator takes as a parameter a single String, which is the name of the event collection the
     * event's being added to. You're responsible for returning a Map which represents the global properties
     * for this particular event collection.
     * <p/>
     * Note that because we use a class defined by you, you can create DYNAMIC global properties. For example,
     * if you want to capture device orientation, then your evaluator can ask the device for its current orientation
     * and then construct the Map. If your global properties aren't dynamic, then just return the same Map
     * every time.
     * <p/>
     * Example usage:
     * <pre>
     *     {@code HatariClient client = HatariClient.client();
     *     GlobalPropertiesEvaluator evaluator = new GlobalPropertiesEvaluator() {
     *         @Override
     *         public Map<String, Object> getGlobalProperties(String eventCollection) {
     *             Map<String, Object> map = new HashMap<String, Object>();
     *             map.put("some dynamic property name", "some dynamic property value");
     *             return map;
     *         }
     *     };
     *     client.setGlobalPropertiesEvaluator(evaluator);
     *     }
     * </pre>
     *
     * @param globalPropertiesEvaluator The evaluator which is invoked any time an event is added to an event
     *                                  collection.
     */
    public void setGlobalPropertiesEvaluator(GlobalPropertiesEvaluator globalPropertiesEvaluator) {
        this.globalPropertiesEvaluator = globalPropertiesEvaluator;
    }

    /**
     * Getter for the HatariClient Global Properties map. See docs for {@link #setGlobalProperties(java.util.Map)}.
     */
    public Map<String, Object> getGlobalProperties() {
        return globalProperties;
    }

    /**
     * Call this to set the Hatari Global Properties Map for this instance of the {@link HatariClient}. The Map
     * is used every time an event is added to an event collection.
     * <p/>
     * Hatari Global Properties are properties which are sent with EVERY event. For example, you may wish to always
     * capture static information like user ID, app version, etc.
     * <p/>
     * Every time an event is added to an event collection, the SDK will check to see if this property is defined.
     * If it is, the SDK will copy all the properties from the global properties into the newly added event.
     * <p/>
     * Note that because this is just a Map, it's much more difficult to create DYNAMIC global properties.
     * It also doesn't support per-collection properties. If either of these use cases are important to you, please use
     * the {@link GlobalPropertiesEvaluator}.
     * <p/>
     * Also note that the Hatari properties defined in {@link #getGlobalPropertiesEvaluator()} take precedence over
     * the properties defined in getGlobalProperties, and that the Hatari Properties defined in each
     * individual event take precedence over either of the Global Properties.
     * <p/>
     * Example usage:
     * <p/>
     * <pre>
     * HatariClient client = HatariClient.client();
     * Map<String, Object> map = new HashMap<String, Object>();
     * map.put("some standard key", "some standard value");
     * client.setGlobalProperties(map);
     * </pre>
     *
     * @param globalProperties The new map you wish to use as the Hatari Global Properties.
     */
    public void setGlobalProperties(Map<String, Object> globalProperties) {
        this.globalProperties = globalProperties;
    }

}
