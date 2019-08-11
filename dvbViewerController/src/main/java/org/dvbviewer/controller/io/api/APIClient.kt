package org.dvbviewer.controller.io.api

import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import org.dvbviewer.controller.data.channel.retrofit.ChannelRootConverterFactory

import org.dvbviewer.controller.data.stream.retrofit.FFMpegConverterFactory
import org.dvbviewer.controller.data.timer.retrofit.TimerConverterFactory
import org.dvbviewer.controller.io.HTTPUtil
import org.dvbviewer.controller.utils.ServerConsts

import retrofit2.Retrofit

object APIClient {

    private var retrofit: Retrofit? = null

    val client: Retrofit
        get() {
            val baseUrl = ServerConsts.REC_SERVICE_URL
            val tikXml = TikXml.Builder()
                    .exceptionOnUnreadXml(false)
                    .build()
            retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(FFMpegConverterFactory.create())
                    .addConverterFactory(TimerConverterFactory.create())
                    .addConverterFactory(ChannelRootConverterFactory.create())
                    .addConverterFactory(TikXmlConverterFactory.create(tikXml))
                    .client(HTTPUtil.getHttpClient())
                    .build()

            return retrofit as Retrofit
        }

}