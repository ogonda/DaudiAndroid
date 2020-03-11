package com.zeroq.daudi4native.ui.preview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.zeroq.daudi4native.R

class PreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        toolbar()
    }

    private fun toolbar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
}
