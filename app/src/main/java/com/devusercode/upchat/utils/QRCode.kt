package com.devusercode.upchat.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

class QRCode {
    companion object {
        fun create(data: String?, width: Int, height: Int): Bitmap? {
            val writer = QRCodeWriter()
            var bitmap: Bitmap? = null

            try {
                val bitMatrix: BitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height)
                bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)

                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap[x, y] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                    }
                }
            } catch (e: WriterException) {
                Log.e("QRCodeGenerator", "Error generating QR code: " + e.message)
            }
            return bitmap
        }
    }
}