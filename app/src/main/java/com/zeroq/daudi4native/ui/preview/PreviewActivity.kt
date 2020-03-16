package com.zeroq.daudi4native.ui.preview

import android.R.attr.data
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import com.google.firebase.storage.StorageReference
import com.zeroq.daudi4native.R
import com.zeroq.daudi4native.commons.BaseActivity
import kotlinx.android.synthetic.main.activity_preview.*
import javax.inject.Inject


class PreviewActivity : BaseActivity() {

    @Inject
    lateinit var storageReference: StorageReference

    private var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        toolbar()

        if (intent.hasExtra(IMAGE_PATH)) {
            path = intent.getStringExtra(IMAGE_PATH)
        }

        displayImage()

    }

    private fun toolbar() {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun displayImage() {
        path?.let {
            val pathReference = storageReference.child(it)

            pathReference.getBytes(ONE_MEGABYTE.toLong()).addOnSuccessListener { data ->
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                ivPhoto.setImageBitmap(bitmap)
            }

        }
    }

    companion object {
        private const val IMAGE_PATH = "IMAGE_PATH"
        private const val ONE_MEGABYTE = 1024 * 1024

        fun startPreviewActivity(context: Context, path: String) {

            val intent = Intent(context, PreviewActivity::class.java)
            intent.putExtra(IMAGE_PATH, path)

            context.startActivity(intent)
        }
    }
}
