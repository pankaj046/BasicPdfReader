package app.pankaj.pdfview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale


class BasicPDFViewer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : PinchZoomRecyclerView(context, attrs) {

    private val TAG = "BasicPDFViewer"
    private lateinit var pdfRenderer: PdfRenderer
    private var currentPage: PdfRenderer.Page? = null
    private var pageCount: Int = 0
    private var pdfFile: File? = null
    private var getPdfFile: GetPdfFile? = null
    private var progressBar: ProgressBar? = null

    init {
        layoutManager = LinearLayoutManager(context)
        getPdfFile = GetPdfFile(context)
    }

    fun setProgressBar(progressBar: ProgressBar) {
        this.progressBar = progressBar
    }

    fun loadPdf(file: Uri) {
        progressBar?.let { visibility = View.VISIBLE }
        pdfFile = getTempFile(file)
        try {
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            pageCount = pdfRenderer.pageCount
            adapter = PdfAdapter()
            scrollToPosition(0)
            progressBar?.let { visibility = View.GONE }
        } catch (e: IOException) {
            e.printStackTrace()
            progressBar?.let { visibility = View.GONE }
        }
    }

    fun loadPdf(url: String) {
        progressBar?.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val deferredPdfFile = async(Dispatchers.IO) {
                    getPdfFile?.downloadPdf(url)
                }
                val pdfFile = deferredPdfFile.await()
                if (pdfFile == null || !pdfFile.exists()) {
                    Log.e("PDFDebug", "PDF file is null or doesn't exist")
                    return@launch
                }
                val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                pdfRenderer = PdfRenderer(fileDescriptor)
                pageCount = pdfRenderer.pageCount
                adapter = PdfAdapter()
                scrollToPosition(0)

            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "loadPdf: "+e.printStackTrace())
            } finally {
                progressBar?.visibility = View.GONE
            }
        }
    }



    fun loadPdf(file: File) {
        progressBar?.let { visibility = View.VISIBLE }
        pdfFile = file
        try {
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            pageCount = pdfRenderer.pageCount
            adapter = PdfAdapter()
            scrollToPosition(0)
            progressBar?.let { visibility = View.GONE }
        } catch (e: IOException) {
            e.printStackTrace()
            progressBar?.visibility = View.GONE
        }
    }


    inner class PdfAdapter : Adapter<PdfViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.item_pdf_page, parent, false)
            return PdfViewHolder(view)
        }

        override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int {
            return pageCount
        }
    }

    inner class PdfViewHolder(itemView: View) : ViewHolder(itemView) {
        private val pdfImageView: ImageView = itemView.findViewById(R.id.pdfImageView)
        private val pageNumberTextView: TextView = itemView.findViewById(R.id.pageNumberTextView)

        fun bind(position: Int) {
            currentPage = pdfRenderer.openPage(position)
            val bitmap = Bitmap.createBitmap(currentPage?.width ?: 0, currentPage?.height ?: 0, Bitmap.Config.ARGB_8888)
            currentPage?.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            pdfImageView.setImageBitmap(bitmap)
            pageNumberTextView.text = String.format(Locale.getDefault(), "${position + 1} / $pageCount")
            currentPage?.close()
        }
    }


    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (::pdfRenderer.isInitialized) {
            pdfRenderer.close()
        }
    }

    private fun getTempFile(file: Uri): File{
        val tempFile = File.createTempFile("temp", ".pdf", context.cacheDir)
        context.contentResolver.openInputStream(file)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }
}
