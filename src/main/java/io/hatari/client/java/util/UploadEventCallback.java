package io.hatari.client.java.util;

/**
 * An interface to simulate functional programming so that the {@link io.hatari.client.java.HatariClient} can
 * notify you when an asynchronous HTTP request succeeds or fails.
 *
 * @author Ebot Tabi
 * @since 1.0.0
 */
public interface UploadEventCallback {
    /**
     * Invoked when adding the event succeeds.
     */
    public void onSuccess();

    /**
     * Invoked when adding the event fails.
     *
     * @param responseBody The HTTP body of the response as a string.
     */
    public void onError(String responseBody);
}

