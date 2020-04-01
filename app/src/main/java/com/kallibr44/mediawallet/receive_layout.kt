package com.kallibr44.mediawallet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import net.glxn.qrgen.android.QRCode

class receive_layout : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_layout)
        val mImagePreview = findViewById(R.id.imagePreview) as ImageView
            val bitmap = QRCode.from(intent.getStringExtra("address")).withSize(1000, 1000).bitmap()
            mImagePreview.setImageBitmap(bitmap)

        }
    }
