package io.hatari.client.java.util;

/**
 * HatariConstants
 *
 * @author Ebot Tabi
 * @since 1.0.0
 */
public class HatariConstants {

    public static final String SERVER_ADDRESS = "https://api.hatario.io";
    public static final String API_VERSION = "1";

    // Hatari API constants

    public static final String NAME_PARAM = "name";
    public static final String DESCRIPTION_PARAM = "description";
    public static final String SUCCESS_PARAM = "success";
    public static final String ERROR_PARAM = "error";
    public static final String INVALID_COLLECTION_NAME_ERROR = "InvalidProjectError";
    public static final String INVALID_PROPERTY_NAME_ERROR = "InvalidPropertyNameError";
    public static final String INVALID_PROPERTY_VALUE_ERROR = "InvalidPropertyValueError";

    // Hatari constants related to how much data we'll cache on the device before aging it out

    // how many events can be stored for a single collection before aging them out
    public static final int MAX_EVENTS_PER_COLLECTION = 10000;
    // how many events to drop when aging out
    public static final int NUMBER_EVENTS_TO_FORGET = 100;
}
