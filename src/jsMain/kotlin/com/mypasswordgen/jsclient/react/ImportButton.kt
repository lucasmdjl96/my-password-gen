/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.jsclient.react

import com.mypasswordgen.jsclient.CssClasses
import com.mypasswordgen.jsclient.dto.DownloadCode
import com.mypasswordgen.jsclient.dto.DownloadSession
import com.mypasswordgen.jsclient.dto.DownloadUser
import com.mypasswordgen.jsclient.externals.DOMException
import com.mypasswordgen.jsclient.externals.QrReaderProps
import com.mypasswordgen.jsclient.externals.autoAnimateRefCallBack
import kotlinx.coroutines.launch
import kotlinx.js.import
import kotlinx.js.jso
import kotlinx.serialization.SerializationException
import org.w3c.dom.HTMLElement
import org.w3c.dom.mediacapture.MediaTrackConstraints
import org.w3c.files.FileReader
import org.w3c.files.get
import react.*
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input

private val qrReader = lazy<QrReaderProps> {
    import("./QrReader.js")
}

external interface ImportButtonProps : Props {
    var importType: ImportExportType
}

private val mainPopup: HTMLElement?
    get() = getHtmlElementById("mainPopup")
private val importButton: HTMLElement?
    get() = getHtmlElementById("importButton")

val ImportButton = FC<ImportButtonProps> { props ->
    var hideScanner by useState(true)
    fun showScanner() {
        hideScanner = false
    }
    fun hideScanner() {
        hideScanner = true
    }

    if (props.importType == ImportExportType.FILE) input {
        id = "importButton"
        hidden = true
        type = InputType.file
        accept = ".json,application/json"
        multiple = false
        value = ""
        onChange = { event ->
            if (event.target.files != null && event.target.files!!.length == 1) {
                val file = event.target.files!![0]!!
                if (file.type == "application/json") {
                    val reader = FileReader()
                    reader.onload = {
                        val text = reader.result as String
                        importFromText(ImportExportType.FILE, text)
                    }
                    reader.readAsText(file)
                }
            }
        }
    }
    if (props.importType == ImportExportType.QR) {
        div {
            className = CssClasses.qrContainerOuter
            div {
                id = "importButton"
                className = if (hideScanner) CssClasses.qrContainerOuter else CssClasses.qrContainerMid
                ref = autoAnimateRefCallBack()
                if (!hideScanner) div {
                    className = CssClasses.qrContainerInner
                    Suspense {
                        this.fallback = ReactNode("Starting QR Scanner...")
                        qrReader {
                            apply(initQrReader())
                        }
                    }
                }
                onClick = {
                    if (hideScanner) {
                        showScanner()
                    } else {
                        hideScanner()
                    }
                }
            }
        }
    }
}

fun initQrReader(): QrReaderProps.() -> Unit = {
    constraints = MediaTrackConstraints(
        facingMode = jso { ideal = "environment" },
        width = jso { ideal = 256 },
        height = jso { ideal = 256 },
        aspectRatio = jso { ideal = 1 }
    )
    containerStyle = jso<dynamic> { width = "256px" }
    videoContainerStyle = jso<dynamic> { width = "100%"; height = "100%" }
    onResult = { resultObj, error ->
        if (error != null && error is DOMException) {
            mainPopup?.dispatchEvent(popupEvent("Import failed. ${error.message}.", PopupType.ERROR))
            ::click on importButton
        } else if (resultObj != null) {
            val text = resultObj.text
            if (text != null) importFromText(ImportExportType.QR, text)
        }
    }
}

fun importFromText(type: ImportExportType, text: String) {
    val code = if (type == ImportExportType.FILE) DownloadCode.fromText(text)
    else DownloadCode.fromShortText(text)
    when (code) {
        DownloadCode.SESSION -> run {
            try {
                val session = DownloadSession.dataFromText(text, pretty = type == ImportExportType.FILE)
                scope.launch {
                    uploadData(session)
                    mainPopup?.dispatchEvent(popupEvent("Import successful.", PopupType.SUCCESS))
                }
            } catch (e: SerializationException) {
                mainPopup?.dispatchEvent(popupEvent("Import failed. Malformed session data.", PopupType.ERROR))
            } finally {
                if (type == ImportExportType.QR) ::click on importButton
            }
        }

        DownloadCode.USER -> run {
            try {
                val user = DownloadUser.dataFromText(text, pretty = type == ImportExportType.FILE)
                scope.launch {
                    uploadData(user)
                    mainPopup?.dispatchEvent(popupEvent("Import successful.", PopupType.SUCCESS))
                }
            } catch (e: SerializationException) {
                mainPopup?.dispatchEvent(popupEvent("Import failed. Malformed user data.", PopupType.ERROR))
            } finally {
                if (type == ImportExportType.QR) ::click on importButton
            }
        }

        null -> run {
            mainPopup?.dispatchEvent(popupEvent("Import failed. Message code not recognized.", PopupType.ERROR))
            if (type == ImportExportType.QR) ::click on importButton
        }
    }
}
