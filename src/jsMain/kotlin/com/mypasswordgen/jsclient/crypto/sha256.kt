/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.jsclient.crypto

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
    return Crypto.Subtle.digest("SHA-512", msgBuffer).then {
        val hashArray = Uint8Array(it).unsafeCast<IntArray>()
        var binaryString = ""
        for (byte in hashArray) {
            binaryString += byte.toChar()
        }
        val truncatedBinaryString = binaryString.substring(0..31)
        return@then btoa(truncatedBinaryString).replace('/', '_').replace('+', '-')
    }.await()
}
