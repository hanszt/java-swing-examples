package hzt.sound;
/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


/**
 * The interface for the JavaSound tabs to open and close audio resources.
 */
public interface ControlContext {
        void open();
        void close();
}
