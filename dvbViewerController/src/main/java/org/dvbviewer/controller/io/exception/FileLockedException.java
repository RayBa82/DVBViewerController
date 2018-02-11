package org.dvbviewer.controller.io.exception;

import java.io.IOException;

public class FileLockedException extends IOException {

    public FileLockedException() {
        super("File is locked");
    }

}
