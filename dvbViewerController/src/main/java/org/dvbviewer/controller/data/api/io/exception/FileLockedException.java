package org.dvbviewer.controller.data.api.io.exception;

import java.io.IOException;

public class FileLockedException extends IOException {

    public FileLockedException() {
        super("File is locked");
    }

}
