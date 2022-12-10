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
import com.mypasswordgen.jsclient.CssClasses
import com.mypasswordgen.jsclient.dto.DownloadSession
import com.mypasswordgen.jsclient.dto.DownloadUser
import com.mypasswordgen.jsclient.externals.QRCodeSVG
import com.mypasswordgen.jsclient.externals.autoAnimateRefCallBack
import org.w3c.dom.HTMLElement
import org.w3c.dom.url.URL
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.useEffect
import react.useState

private val exportButton: HTMLElement?
    get() = getHtmlElementById("exportButton")

external interface ExportButtonProps : Props {
    var loggedIn: Boolean
    var sessionData: FullSessionServerDto?
    var userData: FullUserServerDto?
    var unsetUserData: () -> Unit
    var exportType: ImportExportType
}

val ExportButton = FC<ExportButtonProps> { props ->
    var hideQR by useState(true)
    fun showQR() { hideQR = false }
    fun hideQR() { hideQR = true }

    val sessionAvailable = !props.loggedIn && props.sessionData != null
    val userAvailable = props.loggedIn && props.userData != null

    useEffect(props.sessionData, props.userData) {
        if (sessionAvailable) {
            ::click on exportButton!!
        }
        if (userAvailable) {
            ::click on exportButton!!
            if (props.exportType == ImportExportType.FILE) props.unsetUserData()
        }
    }

    if (props.exportType == ImportExportType.FILE) a {
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
    if (props.exportType == ImportExportType.QR) div {
        className = CssClasses.qrContainerOuter
        div {
            id = "exportButton"
            className = if (hideQR) CssClasses.qrContainerOuter else CssClasses.qrContainerMid
            onClick = {
                if (hideQR) {
                    showQR()
                } else {
                    hideQR()
                }
            }
            ref = autoAnimateRefCallBack()
            if (!hideQR) div {
                className = CssClasses.qrContainerInner
                QRCodeSVG {
                    value =
                        if (sessionAvailable) DownloadSession(props.sessionData!!).toText(pretty = false)
                        else DownloadUser(props.userData!!).toText(pretty = false)
                    size = 256
                    includeMargin = true
                }
            }
        }
    }
}
