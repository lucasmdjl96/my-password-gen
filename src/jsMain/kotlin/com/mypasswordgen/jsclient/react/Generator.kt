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
import com.mypasswordgen.jsclient.autoAnimateRefCallBack
import com.mypasswordgen.jsclient.clipboard
import com.mypasswordgen.jsclient.crypto.sha256
import kotlinx.coroutines.launch
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.clearTimeout
import kotlinx.js.timers.setTimeout
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import react.useState
import kotlin.time.Duration.Companion.seconds

external interface GeneratorProps : Props {
    var username: String
    var masterPassword: String
    var emailAddress: String?
    var siteName: String?
    var password: String?
    var updatePassword: (String) -> Unit
}

val Generator = FC<GeneratorProps> { props ->
    var showPopup: Boolean by useState(false)
    var timeout by useState<Timeout>()
    div {
        className = CssClasses.buttonContainer
        button {
            +"Generate Password"
            id = "passwordGenerator"
            disabled = props.siteName == null
            onClick = {
                scope.launch {
                    props.updatePassword(
                        generatePassword(
                            props.username,
                            props.emailAddress!!,
                            props.siteName!!,
                            props.masterPassword
                        )
                    )
                }
            }
        }
    }
    if (props.password != null) {
        div {
            className = CssClasses.password
            +props.password!!
            button {
                span {
                    className = CssClasses.materialIcon
                    +"content_copy"
                }
                id = "copyButton"
                onClick = {
                    clipboard.writeText(props.password!!)
                    showPopup = true
                    if (timeout != null) clearTimeout(timeout!!)
                    timeout = setTimeout(1.2.seconds) {
                        showPopup = false
                        timeout = null
                    }
                }
            }
            div {
                className = CssClasses.popupContainer
                ref = autoAnimateRefCallBack()
                if (showPopup) span {
                    className = CssClasses.popup
                    +"text copied"
                }
            }
        }
    }
}

suspend fun generatePassword(username: String, emailAddress: String, siteName: String, masterPassword: String) =
    sha256(
        """
            $username
            $emailAddress
            $siteName
            $masterPassword
        """.trimIndent()
    )
