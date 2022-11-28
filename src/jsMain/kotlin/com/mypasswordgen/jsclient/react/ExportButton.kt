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
import com.mypasswordgen.jsclient.dto.DownloadSession
import com.mypasswordgen.jsclient.dto.DownloadUser
import org.w3c.dom.url.URL
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.useEffect

external interface ExportButtonProps : Props {
    var loggedIn: Boolean
    var sessionData: FullSessionServerDto?
    var userData: FullUserServerDto?
    var unsetUserData: () -> Unit
}

val ExportButton = FC<ExportButtonProps> { props ->
    useEffect(props.sessionData, props.userData) {
        if (!props.loggedIn && props.sessionData != null) {
            ::click on getHtmlElementById("exportButton")!!
        }
        if (props.loggedIn && props.userData != null) {
            ::click on getHtmlElementById("exportButton")!!
            props.unsetUserData()
        }
    }
    a {
        id = "exportButton"
        hidden = true
        console.log("loading")
        if (props.sessionData != null) {
            download = "my-password-gen-session.json"
            val file = DownloadSession(props.sessionData!!).toFile()
            href = URL.createObjectURL(file)
        }
        if (props.userData != null) {
            download = "my-password-gen-user.json"
            val file = DownloadUser(props.userData!!).toFile()
            href = URL.createObjectURL(file)
        }
    }
}
