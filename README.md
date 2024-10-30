
# BasicPdfReader

A simple Android library for viewing PDF files using RecyclerView, `PdfRenderer`, and pinch-to-zoom functionality. BasicPdfReader allows users to load PDFs from various sources, including file URIs, HTTP URLs, and `File` objects.

## Features

- Load PDFs from different sources: File URI, HTTP URL, and File objects.
- Simple RecyclerView-based PDF rendering.
- Pinch-to-zoom support
- Smooth PDF page transitions
- Display of page numbers and progress indication

## Installation

Add the following to your `root build.gradle` at the end of the `repositories` block:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

In your app-level `build.gradle`, add the dependency:

```gradle
dependencies {
    implementation 'com.github.pankaj046:BasicPdfReader:0.0.1'
}
```

## Usage

### Basic Setup

1. Include `BasicPDFViewer` in your layout XML file:

    ```xml
    <app.pankaj.pdfview.BasicPDFViewer
        android:id="@+id/pdfViewer"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    ```

2. Initialize `BasicPDFViewer` in your Activity/Fragment and load a PDF from a File URI, HTTP URL, or `File` object:

    ```kotlin
    val pdfViewer = findViewById<BasicPDFViewer>(R.id.pdfViewer)
    pdfViewer.setProgressBar(progressBar) // Optional: Show loading progress
    pdfViewer.loadPdf(fileUri) // Load from file URI
    pdfViewer.loadPdf("https://example.com/sample.pdf") // Load from HTTP URL
    pdfViewer.loadPdf(pdfFile) // Load from File object
    ```

### Customizing

- `setProgressBar(progressBar: ProgressBar)`: Assigns a ProgressBar for loading indication.
- `PdfAdapter`: Handles PDF pages using RecyclerView with `PdfViewHolder`.
