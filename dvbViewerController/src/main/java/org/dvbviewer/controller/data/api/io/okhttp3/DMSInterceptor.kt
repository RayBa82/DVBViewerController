package org.dvbviewer.controller.data.api.io.okhttp3

import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.Response
import org.apache.commons.lang3.StringUtils
import org.dvbviewer.controller.data.api.io.exception.*
import org.dvbviewer.controller.utils.ServerConsts.*
import org.dvbviewer.controller.utils.URLUtil
import java.io.IOException

class DMSInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (StringUtils.isBlank(REC_SERVICE_HOST)) {
            throw NoHostException()
        }
        val request = chain.request()
        val requestUrl = request.url()
        val modifiedUrl = URLUtil.replaceUrl(requestUrl)!!
                .build() ?: throw InvalidHostException(REC_SERVICE_HOST)
        val credentials = Credentials.basic(REC_SERVICE_USER_NAME, REC_SERVICE_PASSWORD)
        val modifiedRequest = request
                .newBuilder()
                .url(modifiedUrl)
                .addHeader(HEADER_AUTHORIZATION, credentials)
                .addHeader(HEADER_CONNECTION, "keep-alive")
                .build()

        val response = chain.proceed(modifiedRequest)
        if (!response.isSuccessful) {
            val e: IOException
            when (response.code()) {
                401 -> e = AuthenticationException()
                423 -> e = FileLockedException()
                else -> e = UnsuccessfulHttpException(response.code())
            }
            throw DefaultHttpException(modifiedUrl.toString(), e)
        }
        return response
    }

    companion object {


        const val HEADER_AUTHORIZATION = "Authorization"
        const val HEADER_CONNECTION = "Connection"
    }

}