package org.dvbviewer.controller.io;

/**
 * Created by rbaun on 22.11.15.
 */
public class UnsuccessfullHttpException extends Exception {

    public UnsuccessfullHttpException(int statusCode) {
        super("HTTP Code: " + statusCode);
    }
}
