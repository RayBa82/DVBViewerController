package org.dvbviewer.controller.io.exception;

public class NoHostException extends IllegalStateException {

    public NoHostException() {
        super("No Host found in configuration");
    }

}
