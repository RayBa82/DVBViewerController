package org.dvbviewer.controller.io.exception;

import java.io.IOException;

public class AuthenticationException extends IOException {

    public AuthenticationException() {
        super("Authentication error");
    }

}
