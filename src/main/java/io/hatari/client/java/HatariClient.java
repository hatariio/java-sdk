package io.hatari.client.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.hatari.client.java.exceptions.InvalidEventException;
import io.hatari.client.java.exceptions.InvalidEventException;
import io.hatari.client.java.exceptions.HatariException;

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
 *     HatariClient.initialize("my_project_token");
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
}
