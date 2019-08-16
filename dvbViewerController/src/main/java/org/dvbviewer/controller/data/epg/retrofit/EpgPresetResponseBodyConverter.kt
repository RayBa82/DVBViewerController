package org.dvbviewer.controller.data.epg.retrofit


import okhttp3.ResponseBody
import org.dvbviewer.controller.entities.EpgEntry
import org.dvbviewer.controller.io.data.EpgEntryHandler
import retrofit2.Converter

class EpgPresetResponseBodyConverter : Converter<ResponseBody, List<EpgEntry>> {

    private val prefsHandler = EpgEntryHandler()

    override fun convert(value: ResponseBody): List<EpgEntry>? {
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