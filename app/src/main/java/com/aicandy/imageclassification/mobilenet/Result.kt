package com.aicandy.imageclassification.mobilenet


import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import java.io.File

class Result : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val imagePath = intent.getStringExtra("image_path")
        val pred = intent.getStringExtra("pred")
        Log.d(
            TAG,
            "Hiển thị kết quả phân loại: $pred"
        )
        if (imagePath != null) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                val imageView = findViewById<ImageView>(R.id.image)
                imageView.setImageBitmap(bitmap)
            } else {
                Log.e(TAG, "Không tìm thấy file ảnh")
            }
        }
        val textView = findViewById<TextView>(R.id.label)
        textView.text = pred
    }

    companion object {
        private const val TAG = "Result"
    }
}