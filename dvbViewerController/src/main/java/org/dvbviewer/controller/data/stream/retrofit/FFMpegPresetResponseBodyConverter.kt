package org.dvbviewer.controller.data.stream.retrofit


import okhttp3.ResponseBody
import org.dvbviewer.controller.data.entities.FFMpegPresetList
import org.dvbviewer.controller.io.data.FFMPEGPrefsHandler
import retrofit2.Converter

class FFMpegPresetResponseBodyConverter : Converter<ResponseBody, FFMpegPresetList> {

    private val prefsHandler = FFMPEGPrefsHandler()

    override fun convert(value: ResponseBody): FFMpegPresetList? {
        try {
            return prefsHandler.parse(value.string())
        } catch (e: RuntimeException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            value.close()
        }
    }

}