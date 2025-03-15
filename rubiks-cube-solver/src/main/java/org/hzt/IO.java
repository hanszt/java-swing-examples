package org.hzt;

import java.net.URL;
import java.util.Objects;

public final class IO {

    public static URL resourceUrl(final String name) {
        return Objects.requireNonNull(IO.class.getResource(name), "resource not found: " + name);
    }
}
