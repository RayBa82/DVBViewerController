package org.dvbviewer.controller.data.api.io.exception;

public class NoHostException extends IllegalStateException {

    public NoHostException() {
        super("No Host found in configuration");
    }

}
