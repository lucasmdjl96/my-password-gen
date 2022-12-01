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

import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import com.mypasswordgen.jsclient.QRCodeSVG
import com.mypasswordgen.jsclient.dto.DownloadSession
import com.mypasswordgen.jsclient.dto.DownloadUser
import kotlinx.browser.window
import kotlinx.js.jso
import org.w3c.dom.url.URL
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState

external interface ExportButtonProps : Props {
    var loggedIn: Boolean
    var sessionData: FullSessionServerDto?
    var userData: FullUserServerDto?
    var unsetUserData: () -> Unit
    var exportType: ExportType?
}

val ExportButton = FC<ExportButtonProps> { props ->
    var showQR by useState(false)

    val sessionAvailable = !props.loggedIn && props.sessionData != null
    val userAvailable = props.loggedIn && props.userData != null

    useEffect(props.sessionData, props.userData) {
            if (sessionAvailable) {
                ::click on getHtmlElementById("exportButton")!!
            }
            if (userAvailable) {
                ::click on getHtmlElementById("exportButton")!!
                if (props.exportType == ExportType.FILE) props.unsetUserData()
            }
    }
    if (props.exportType == ExportType.FILE) a {
        id = "exportButton"
        hidden = true
        if (sessionAvailable) {
            download = "my-password-gen-session.json"
            val file = DownloadSession(props.sessionData!!).toFile()
            href = URL.createObjectURL(file)
        }
        if (userAvailable) {
            download = "my-password-gen-user.json"
            val file = DownloadUser(props.userData!!).toFile()
            href = URL.createObjectURL(file)
        }
    }
    if (props.exportType == ExportType.QR) div {
        hidden = !sessionAvailable && !userAvailable || !showQR
        id = "exportButton"
        onClick = { event ->
            event.stopPropagation()
            if (showQR) showQR = false
            else {
                showQR = true
                window.document.addEventListener("click", {
                    showQR = false
                }, jso { once = true })
            }
        }
        if (sessionAvailable || userAvailable) QRCodeSVG {
            value = if (sessionAvailable) DownloadSession(props.sessionData!!).toText(pretty = false)
            else DownloadUser(props.userData!!).toText(pretty = false)
        }
    }
}

enum class ExportType {
    FILE, QR;
}
