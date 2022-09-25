package crypto

import kotlinx.coroutines.await
import kotlinx.js.ArrayBuffer
import kotlinx.js.Uint8Array
import kotlin.js.Promise

@JsName("crypto")
external object Crypto {
    @JsName("subtle")
    object Subtle {
        fun digest(algorithm: String, buffer: Uint8Array): Promise<ArrayBuffer>
    }
}

external fun btoa(string: String): String

external class TextEncoder {
    fun encode(message: String): Uint8Array
}

suspend fun sha256(message: String): String {
    val msgBuffer = TextEncoder().encode(message)
    return Crypto.Subtle.digest("SHA-256", msgBuffer).then {
        val hashArray = Uint8Array(it).unsafeCast<IntArray>()
        var binaryString = ""
        for (byte in hashArray) {
            binaryString += byte.toChar()
        }
        return@then btoa(binaryString).replace('/', '_').replace('+', '-')
    }.await()
}