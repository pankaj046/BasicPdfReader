package app.pankaj.pdfview

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

class GetPdfFile(private val context: Context) {

    private val okHttpClient = OkHttpClient()

    suspend fun downloadPdf(url: String): File? {
        return withContext(Dispatchers.IO) {
            val cacheDir = context.cacheDir
            val file = File(cacheDir, getFileNameFromUrl(url))

            if (file.exists()) {
                return@withContext file
            }

            val request = Request.Builder().url(url).build()

            try {
                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    response.body?.byteStream()?.use { inputStream ->
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    return@withContext file
                } else {
                    Log.e("PDFDownload", "Failed to download file: ${response.message}, code: ${response.code}")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("PDFDownload", e.printStackTrace().toString())
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    private fun getFileNameFromUrl(url: String): String {
        return Uri.parse(url).lastPathSegment ?: "${System.currentTimeMillis()}.pdf"
    }
}


