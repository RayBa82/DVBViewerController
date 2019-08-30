package org.dvbviewer.controller.data.api.io.exception

import java.io.IOException

class UnsuccessfulHttpException(statusCode: Int) : IOException("HTTP Code: $statusCode")
