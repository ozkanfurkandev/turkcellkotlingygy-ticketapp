package com.turkcell.ticketapp.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

@Composable
fun QrCodeImage(
    content: String,
    modifier: Modifier = Modifier,
    sizePx: Int = 512,
) {
    val imageBitmap = remember(content, sizePx) {
        runCatching {
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE,
                    )
                }
            }
            bitmap.asImageBitmap()
        }.getOrNull()
    }

    if (imageBitmap != null) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Bilet QR kodu",
            modifier = modifier,
        )
    }
}
