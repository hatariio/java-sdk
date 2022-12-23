package io.hatari.client.java.data;

import java.util.Map;

/**
 * An interface to simulate functional programming so that you can tell the {@link io.hatari.client.java.HatariClient}
 * how to dynamically return Keen Global Properties based on event collection name.
 *
 * @author dkador
 * @since 1.0.0
 */
public interface GlobalPropertiesEvaluator {
    Map<String, Object> getGlobalProperties(String eventCollection);
}