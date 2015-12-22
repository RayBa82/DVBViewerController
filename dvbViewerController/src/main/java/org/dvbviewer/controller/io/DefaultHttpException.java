package org.dvbviewer.controller.io;

/**
 * Created by rbaun on 22.11.15.
 */
public class DefaultHttpException extends Exception {

    public DefaultHttpException(String url) {
        super("Error accessing url: "+url);
    }
}
