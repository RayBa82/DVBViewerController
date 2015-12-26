package org.dvbviewer.controller.io;

/**
 * Created by rbaun on 22.11.15.
 */
public class DefaultHttpException extends Exception {

    public DefaultHttpException(String url, Throwable cause) {
        super("Error accessing url: "+url + " cause:" + cause.getClass().getSimpleName());
    }
}
