/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.jsclient.dto

import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

val prettyJsonFormatter = Json {
    prettyPrint = true
}

private const val codeName = "code"

@Serializable
enum class DownloadCode(val short: String) {
    SESSION("S"),
    USER("U"),
    ;

    companion object {
        private val fromShort = buildMap {
            for (code in DownloadCode.values()) {
                put(code.short, code)
            }
        }
        fun fromText(text: String): DownloadCode? {
            val codes = DownloadCode.values().joinToString("|", "(", ")")
            val regex = Regex(
                """
                    "$codeName": "$codes"
                """.trimIndent()
            )
            val (code) = regex.find(text)?.destructured ?: return null
            return DownloadCode.valueOf(code)
        }

        fun fromShortText(text: String): DownloadCode? {
            val shortCodes = DownloadCode.values().joinToString("|", "^(", ")") { it.short }
            val regex = Regex(shortCodes)
            val (shortCode) = regex.find(text)?.destructured ?: return null
            return fromShort[shortCode]
        }
    }

}

@Serializable
class DownloadSession(override val data: FullSessionServerDto) : DownloadFile<FullSessionServerDto> {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName(codeName)
    override val downloadCode: DownloadCode = DownloadCode.SESSION
    override fun toText(pretty: Boolean) =
        if (pretty) prettyJsonFormatter.encodeToString(this)
        else UglySerializer.serialize(this)
    override fun toFile() = Blob(
        arrayOf(toText()),
        BlobPropertyBag(type = "application/json")
    )
    companion object {
        fun fromText(text: String, pretty: Boolean = true): DownloadSession =
            if (pretty) prettyJsonFormatter.decodeFromString(text)
            else UglySerializer.deserializeSession(text)
        fun dataFromText(text: String) = fromText(text).data
    }
}

@Serializable
class DownloadUser(override val data: FullUserServerDto) : DownloadFile<FullUserServerDto> {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    @SerialName(codeName)
    override val downloadCode: DownloadCode = DownloadCode.USER
    override fun toText(pretty: Boolean) =
        if (pretty) prettyJsonFormatter.encodeToString(this)
        else UglySerializer.serialize(this)
    override fun toFile() = Blob(
        arrayOf(toText()),
        BlobPropertyBag(type = "application/json")
    )
    companion object {
        fun fromText(text: String, pretty: Boolean = true): DownloadUser =
            if (pretty) prettyJsonFormatter.decodeFromString(text)
            else UglySerializer.deserializeUser(text)
        fun dataFromText(text: String) = fromText(text).data
    }
}

@Serializable
sealed interface DownloadFile<T> {
    val data: T
    val downloadCode: DownloadCode

    fun toText(pretty: Boolean = true): String
    fun toFile(): Blob/* = Blob(
        arrayOf(jsonFormatter.encodeToString(this)),
        BlobPropertyBag(type = "application/json")
    )*/
}
