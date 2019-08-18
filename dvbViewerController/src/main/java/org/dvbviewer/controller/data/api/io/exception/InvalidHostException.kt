package org.dvbviewer.controller.data.api.io.exception

class InvalidHostException(host : String) : IllegalArgumentException("Invalid Host '$host' found in configuration")