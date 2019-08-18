package org.dvbviewer.controller.data.api.io.exception;

import java.io.IOException;

public class DefaultHttpException extends IOException {

    public DefaultHttpException(String url, Throwable cause) {
        super(cause.getMessage()
                + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "when accessing url:"
                + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + url);
    }

}
