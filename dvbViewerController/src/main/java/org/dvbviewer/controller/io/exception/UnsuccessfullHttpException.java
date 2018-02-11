package org.dvbviewer.controller.io.exception;

import java.io.IOException;

public class UnsuccessfullHttpException extends IOException {

    public UnsuccessfullHttpException(int statusCode) {
        super("HTTP Code: " + statusCode);
    }

}
