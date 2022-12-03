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
import com.mypasswordgen.jsclient.DOMException
import com.mypasswordgen.jsclient.QrReader
import com.mypasswordgen.jsclient.QrReaderProps
import com.mypasswordgen.jsclient.dto.DownloadCode
import com.mypasswordgen.jsclient.dto.DownloadSession
import com.mypasswordgen.jsclient.dto.DownloadUser
import kotlinx.coroutines.launch
import kotlinx.js.jso
import kotlinx.serialization.SerializationException
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.mediacapture.MediaTrackConstraints
import org.w3c.files.FileReader
import org.w3c.files.get
import react.FC
import react.Props
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useState

external interface ImportButtonProps : Props {
    var exportType: ExportType
}

private val successPopup: HTMLElement?
    get() = getHtmlElementById("successPopup")
private val errorPopup: HTMLElement?
    get() = getHtmlElementById("errorPopup")
private val qrScannerContainer: HTMLElement?
    get() = getHtmlElementById("qrScannerContainer")

val ImportButton = FC<ImportButtonProps> { props ->
    var hideScanner by useState(true)
    fun showScanner() {
        hideScanner = false
    }

    fun hideScanner() {
        hideScanner = true
    }

    if (props.exportType == ExportType.FILE) input {
        id = "importButton"
        hidden = true
        type = InputType.file
        accept = ".json,application/json"
        multiple = false
        value = ""
        onChange = ::handleFileChangeEvent
    }
    if (props.exportType == ExportType.QR) {
        div {
            hidden = hideScanner
            className = CssClasses.qrContainerOuter
            div {
                id = "qrScannerContainer"
                className = CssClasses.qrContainerMid
                div {
                    className = CssClasses.qrContainerInner
                    id = "importButton"
                    if (!hideScanner) QrReader {
                        apply(initQrReader())
                    }
                    onClick = {
                        if (hideScanner) {
                            showScanner()
                            qrScannerContainer!!.addEventListenerOnceWhen("click", Event::wasFiredHere) {
                                hideScanner()
                            }
                        }
                    }
                }
            }
        }
    }
}

fun handleFileChangeEvent(event: ChangeEvent<HTMLInputElement>) {
    if (event.target.files != null && event.target.files!!.length == 1) {
        val file = event.target.files!![0]!!
        if (file.type == "application/json") {
            val reader = FileReader()
            reader.onload = {
                val text = reader.result as String
                when (DownloadCode.fromText(text)) {
                    DownloadCode.SESSION -> run {
                        try {
                            val session = DownloadSession.dataFromText(text)
                            scope.launch {
                                uploadData(session)
                                successPopup?.innerText = "Import successful."
                                ::click on successPopup
                            }
                        } catch (e: SerializationException) {
                            errorPopup?.innerText = "Import failed. Malformed session data."
                            ::click on errorPopup
                        }
                    }

                    DownloadCode.USER -> run {
                        try {
                            val user = DownloadUser.dataFromText(text)
                            scope.launch {
                                uploadData(user)
                                successPopup?.innerText = "Import successful."
                                ::click on successPopup
                            }
                        } catch (e: SerializationException) {
                            errorPopup?.innerText = "Import failed. Malformed user data."
                            ::click on errorPopup
                        }
                    }

                    null -> run {
                        errorPopup?.innerText = "Import failed. Message code not recognized."
                        ::click on errorPopup
                    }
                }
            }
            reader.readAsText(file)
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
            errorPopup?.innerText = "Import failed. ${error.message}."
            ::click on errorPopup
            ::click on qrScannerContainer
        } else if (resultObj != null) {
            val result = resultObj.text
            console.log(result)
            if (result != null) when (DownloadCode.fromShortText(result)) {
                DownloadCode.SESSION -> run {
                    try {
                        val session =
                            DownloadSession.dataFromText(result, pretty = false)
                        scope.launch {
                            uploadData(session)
                            successPopup?.innerText = "Import successful."
                            ::click on successPopup
                        }
                    } catch (e: SerializationException) {
                        errorPopup?.innerText =
                            "Import failed. Malformed session data."
                        ::click on errorPopup
                    } finally {
                        ::click on qrScannerContainer
                    }
                }

                DownloadCode.USER -> run {
                    try {
                        val user = DownloadUser.dataFromText(result, pretty = false)
                        scope.launch {
                            uploadData(user)
                            successPopup?.innerText = "Import successful."
                            ::click on successPopup
                        }
                    } catch (e: SerializationException) {
                        errorPopup?.innerText = "Import failed. Malformed user data."
                        ::click on errorPopup
                    } finally {
                        ::click on qrScannerContainer
                    }
                }

                null -> run {
                    errorPopup?.innerText =
                        "Import failed. Message code not recognized."
                    ::click on errorPopup
                    ::click on qrScannerContainer
                }
            }
        }
    }
}
