package org.dvbviewer.controller.data.channel.retrofit


import okhttp3.ResponseBody
import org.dvbviewer.controller.data.api.handler.ChannelHandler
import org.dvbviewer.controller.data.entities.ChannelRoot
import retrofit2.Converter

class ChannelRootPresetResponseBodyConverter : Converter<ResponseBody, List<ChannelRoot>> {

    private val prefsHandler = ChannelHandler()

    override fun convert(value: ResponseBody): List<ChannelRoot>? {
        try {
            return prefsHandler.parse(value.byteStream())
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            value.close()
        }
    }

}