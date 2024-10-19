package com.aicandy.imageclassification.mobilenet


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.TextView
import com.aicandy.imageclassification.mobilenet.Utils.assetFilePath
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Math.min
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private var classifier: Classifier? = null
    private var statusText: TextView? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        statusText = findViewById<TextView>(R.id.statusText)
        classifier = Classifier(assetFilePath(this, "mobilenet-v2.pt"))
        processImage("Flamingo.jpg")
    }

    private fun processImage(fileName: String) {
        updateStatus("Đang xử lý ảnh...")
        executor.execute {
            try {
                // Đọc ảnh
                updateStatus("Đang đọc ảnh...")
                val originalBitmap = loadImageFromAssets(fileName)
                    ?: throw IOException("Không thể đọc ảnh từ assets")

                // Resize ảnh nếu cần
                updateStatus("Đang resize ảnh...")
                val resizedBitmap = resizeBitmap(originalBitmap)

                // Phân loại ảnh
                updateStatus("Đang phân loại ảnh...")
                val prediction: String? = classifier?.predict(originalBitmap)

                // Lưu ảnh đã resize
                val tempImagePath = saveBitmapToTempFile(resizedBitmap)

                // Dọn dẹp bộ nhớ
                originalBitmap.recycle()
                if (resizedBitmap != originalBitmap) {
                    resizedBitmap.recycle()
                }

                // Chuyển sang màn hình kết quả
                if (prediction != null) {
                    showResult(tempImagePath, prediction)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Lỗi xử lý ảnh", e)
                showError("Lỗi: " + e.message)
            }
        }
    }

    private fun updateStatus(status: String) {
        mainHandler.post {
            statusText!!.visibility = View.VISIBLE
            statusText!!.text = status
        }
    }

    private fun showResult(imagePath: String, prediction: String) {
        mainHandler.post {
            statusText!!.visibility = View.GONE
            val resultView = Intent(this@MainActivity, Result::class.java)
            resultView.putExtra("image_path", imagePath)
            resultView.putExtra("pred", prediction)
            startActivity(resultView)
        }
    }

    private fun showError(error: String) {
        mainHandler.post {
            statusText!!.text = error
            statusText!!.visibility = View.VISIBLE
        }
    }


    private fun loadImageFromAssets(fileName: String): Bitmap? {
        var inputStream: InputStream? = null
        return try {
            inputStream = assets.open(fileName)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeStream(inputStream, null, options)
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Lỗi khi đóng inputStream", e)
                }
            }
        }
    }

    private fun resizeBitmap(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val scale = min(
            (
                    MAX_IMAGE_DIMENSION.toFloat() / width).toDouble(),
            (
                    MAX_IMAGE_DIMENSION.toFloat() / height).toDouble()
        ).toFloat()
        if (scale >= 1) return original
        val newWidth = Math.round(width * scale)
        val newHeight = Math.round(height * scale)
        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true)
    }


    private fun saveBitmapToTempFile(bitmap: Bitmap): String {
        val tempFile = File.createTempFile("temp_image", ".jpg", cacheDir)
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(tempFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: IOException) {
                    Log.e(TAG, "Lỗi khi đóng FileOutputStream", e)
                }
            }
        }
        return tempFile.absolutePath
    }

    override fun onDestroy() {
        super.onDestroy()
        executor.shutdown()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val MAX_IMAGE_DIMENSION = 1024
    }
}