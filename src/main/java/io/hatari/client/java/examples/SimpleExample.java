package io.hatari.client.java.examples;

import io.hatari.client.java.HatariClient;
import io.hatari.client.java.exceptions.HatariException;

import java.util.HashMap;
import java.util.Map;

public class SimpleExample {

    public static void main(String[] args) throws HatariException {

        Map<String, Object> myEvent = new HashMap<>();
        myEvent.put("property name", "property value");
        myEvent.put("property name", "property value");

        HatariClient.initialize("project_token", "");

        HatariClient.client().addEvent("transactions", myEvent);
    }
}
