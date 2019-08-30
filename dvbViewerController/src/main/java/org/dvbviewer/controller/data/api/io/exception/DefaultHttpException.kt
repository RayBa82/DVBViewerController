package org.dvbviewer.controller.data.api.io.exception

import java.io.IOException

class DefaultHttpException(url: String, cause: Throwable) : IOException(cause.message
        + System.getProperty("line.separator")
        + System.getProperty("line.separator")
        + "when accessing url:"
        + System.getProperty("line.separator")
        + System.getProperty("line.separator")
        + url)
