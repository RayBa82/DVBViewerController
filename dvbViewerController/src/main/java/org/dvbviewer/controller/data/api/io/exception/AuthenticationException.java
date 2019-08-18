package org.dvbviewer.controller.data.api.io.exception;

import java.io.IOException;

public class AuthenticationException extends IOException {

    public AuthenticationException() {
        super("Authentication error");
    }

}
