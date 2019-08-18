package org.dvbviewer.controller.data.api

import org.dvbviewer.controller.data.api.ApiStatus.*

class ApiResponse<T>(val status: ApiStatus, val data: T?, val message: String?, val e: Exception?) {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val resource = o as ApiResponse<*>?

        if (status != resource!!.status) {
            return false
        }
        if (if (message != null) message != resource.message else resource.message != null) {
            return false
        }
        return if (data != null) data == resource.data else resource.data == null
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Resource{" +
                "status=" + status +
                ", message='" + message + '\''.toString() +
                ", data=" + data +
                '}'.toString()
    }

    companion object {

        fun <T> success(data: T?): ApiResponse<T> {
            return ApiResponse(SUCCESS, data, null, null)
        }

        fun <T> error(e: Exception?, data: T?): ApiResponse<T> {
            return ApiResponse(ERROR, data, null, e)
        }

        fun <T> notSupported(msg: String): ApiResponse<T> {
            return ApiResponse(NOT_SUPPORTED, null, msg, null)
        }

        fun <T> loading(data: T?): ApiResponse<T> {
            return ApiResponse(LOADING, data, null, null)
        }
    }
}
