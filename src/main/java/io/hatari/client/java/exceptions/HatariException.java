package io.hatari.client.java.exceptions;

/**
 * HatariException
 *
 * @author Ebot Tabi
 * @since 1.0.0
 */
public abstract class HatariException extends Exception {

    HatariException(String detailMessage) {
        super(detailMessage);
    }
}
