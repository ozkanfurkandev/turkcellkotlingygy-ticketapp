package com.turkcell.core.util

import com.turkcell.core.network.ApiException
import com.turkcell.core.network.NetworkException

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> toApiMessage()
    is NetworkException -> "İnternet bağlantısı yok"
    else -> message ?: "Bilinmeyen bir hata oluştu."
}

private fun ApiException.toApiMessage(): String = when (code) {
    400 -> "Geçersiz istek"
    401 -> "Oturum süresi doldu veya giriş bilgileri hatalı"
    403 -> "Bu işlem için yetkiniz yok"
    404 -> "Kayıt bulunamadı"
    409 -> toConflictMessage()
    in 500..599 -> "Sunucu şu anda cevap veremiyor"
    else -> errorMessage ?: "Beklenmeyen bir hata oluştu"
}

private fun ApiException.toConflictMessage(): String {
    val body = errorMessage?.lowercase().orEmpty()
    return when {
        body.contains("email_taken") -> "Bu email zaten kayıtlı"
        body.contains("capacity_exceeded") -> "Stok yetersiz, yenile"
        body.contains("already_paid") -> "Bu satın alım zaten ödenmiş"
        else -> errorMessage ?: "İşlem şu an gerçekleştirilemiyor"
    }
}

fun Throwable.toLoginMessage(): String = when (this) {
    is ApiException -> when (code) {
        401 -> "Email veya şifre hatalı"
        in 500..599 -> "Sunucu şu anda cevap veremiyor"
        else -> toUserMessage()
    }
    else -> toUserMessage()
}

fun Throwable.toRegisterMessage(): String = when (this) {
    is ApiException -> when (code) {
        409 -> "Bu email zaten kayıtlı"
        400 -> "Geçersiz email veya şifre formatı"
        in 500..599 -> "Sunucu şu anda cevap veremiyor"
        else -> toUserMessage()
    }
    else -> toUserMessage()
}

fun Throwable.toPurchaseMessage(): String = when (this) {
    is ApiException -> when (code) {
        409 -> toConflictMessage()
        403 -> "Bu satın alım size ait değil"
        404 -> "Satın alım bulunamadı"
        else -> toUserMessage()
    }
    else -> toUserMessage()
}

fun Throwable.toCheckinMessage(): String = when (this) {
    is ApiException -> when (code) {
        404 -> "Bilet bulunamadı"
        403 -> "Bu etkinliğe atanmamışsınız"
        409 -> {
            val body = errorMessage?.lowercase().orEmpty()
            if (body.contains("already_used")) "Bu bilet kullanılmış" else toConflictMessage()
        }
        else -> toUserMessage()
    }
    else -> toUserMessage()
}
