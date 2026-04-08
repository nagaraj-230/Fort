package com.payoda.rootchecker;

import com.payoda.rootchecker.util.QLog;

public class SourceIDENative {

    static boolean libraryLoaded = false;

    /**
     * Loads the C/C++ libraries statically
     */
    static {
        try {
            System.loadLibrary("tool-checker");
            libraryLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            QLog.e(e);
        }
    }

    public boolean wasNativeLibraryLoaded() {
        return libraryLoaded;
    }

    public native int checkForSource(Object[] pathArray);

    public native int setLogDebugMessages(boolean logDebugMessages);

}
