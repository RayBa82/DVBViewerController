package org.dvbviewer.controller.data.api.io.exception;

import java.io.IOException;

public class UnsuccessfullHttpException extends IOException {

    public UnsuccessfullHttpException(int statusCode) {
        super("HTTP Code: " + statusCode);
    }

}
