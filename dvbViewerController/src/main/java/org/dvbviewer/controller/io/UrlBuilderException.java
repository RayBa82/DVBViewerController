package org.dvbviewer.controller.io;

/**
 * Created by rbaun on 22.01.16.
 */
public class UrlBuilderException extends Exception {

    public UrlBuilderException(String url) {
        super("Could nor create URL from " + url);
    }
}
