package org.dvbviewer.controller.io;

/**
 * Created by rbaun on 22.11.15.
 */
public class DefaultHttpException extends Exception {

    private Throwable throwable;

    public DefaultHttpException(String url, Throwable cause) {
        super(cause.getMessage()
                + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "when accessing url:"
                + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + url);
        throwable = cause;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
