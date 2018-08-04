package org.dvbviewer.controller.io.exception

class InvalidHostException(host : String) : IllegalArgumentException("Invalid Host '$host' found in configuration")