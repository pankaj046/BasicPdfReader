package app.pankaj.pdfreader

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import app.pankaj.pdfview.BasicPDFViewer
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private var basicPDFViewer : BasicPDFViewer?=null
    private var progress : ProgressBar?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        basicPDFViewer  = findViewById(R.id.pdf)
        progress  = findViewById(R.id.progress)

        basicPDFViewer?.setProgressBar(progress!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_pdf) {
            openFile()
            return true
        }
        if (id ==  R.id.action_link_pdf){
            showUrlInputDialog(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openFile() {
        pickPdfFile.launch(arrayOf("application/pdf"))
    }

    private val pickPdfFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
             basicPDFViewer?.loadPdf(it)
        } ?: run {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUrlInputDialog(context: Context) {
        val input = EditText(context)
        input.hint = "Enter HTTPS URL"

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)
        layout.addView(input)

        val dialog = AlertDialog.Builder(context)
            .setTitle("Enter URL")
            .setView(layout)
            .setPositiveButton("Submit") { dialog, _ ->
                val url = input.text.toString()
                if (url.startsWith("https://")) {
                    lifecycleScope.launch { basicPDFViewer?.loadPdf(url) }
                } else {
                    Toast.makeText(context, "Please enter a valid HTTPS URL", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
        dialog.show()
    }
}