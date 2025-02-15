package org.hzt;

import java.net.URL;

public final class Resources {

    public static URL urlOrThrow(String path) {
        final var url = Resources.class.getResource(path);
        if (url == null) {
            throw new IllegalStateException("Resource not found at " + path);
        }
        return url;
    }
}
