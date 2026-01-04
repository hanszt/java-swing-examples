package org.hzt;

import java.net.URL;
import java.util.Objects;

public final class IO {

    private IO() {
        throw new AssertionError("Cannot instantiate IO");
    }

    public static URL resourceUrl(final String name) {
        return Objects.requireNonNull(IO.class.getResource(name), "resource not found: " + name);
    }
}
