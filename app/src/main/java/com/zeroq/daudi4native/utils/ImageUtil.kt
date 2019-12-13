package com.zeroq.daudi4native.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Environment
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt


class ImageUtil @Inject constructor() {

    fun dpToPx(context: Context, dp: Int): Int {
        val r = context.resources
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics).roundToInt()
    }

    fun reactiveTakeScreenShot(parent: ViewGroup): Observable<Boolean> {
        return Observable.just(takeandSaveScreenShot(parent))
    }

    fun takeandSaveScreenShot(parent: ViewGroup): Boolean {
        val u = parent as View
        val z = parent

        val totalHeight = z.getChildAt(0).height
        val totalWidth = z.getChildAt(0).width

        val newHeight = totalHeight * 537 / totalWidth

        val b = getBitmapFromView(u, totalHeight, totalWidth)
        return savescreenshot(b, 537, newHeight)
    }


    private fun getBitmapFromView(view: View, totalHeight: Int, totalWidth: Int): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background

        if (bgDrawable != null)
            bgDrawable.draw(canvas)
        else
            canvas.drawColor(Color.WHITE)

        view.draw(canvas)
        return returnedBitmap
    }


    private fun savescreenshot(bm: Bitmap, newWidth: Int, newHeight: Int): Boolean {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()

        val file_path = Environment.getExternalStorageDirectory().absolutePath + "/Emkaynow"
        val dir = File(file_path)


        if (!dir.exists()) {
            dir.mkdirs()
        }
        val image = File(dir, "0.png")
        var out: FileOutputStream? = null

        try {
            out = FileOutputStream(image)
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
            return true
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }
}