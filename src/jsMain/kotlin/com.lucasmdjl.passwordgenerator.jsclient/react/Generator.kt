package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import com.lucasmdjl.passwordgenerator.jsclient.clipboard
import com.lucasmdjl.passwordgenerator.jsclient.crypto.sha256
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
    var online: Boolean
}

val Generator = FC<GeneratorProps> { props ->
    var showPopup: Boolean by useState(false)
    var timeout by useState<Timeout>()
    div {
        className = CssClasses.buttonContainer
        button {
            +"Generate Password"
            disabled = props.siteName == null
            onClick = {
                scope.launch {
                    props.updatePassword(
                        generatePassword(
                            if (props.online) props.username
                            else encodeUsername(props.username),
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
                onClick = {
                    clipboard.writeText(props.password!!)
                    showPopup = true
                    if (timeout != null) clearTimeout(timeout!!)
                    timeout = setTimeout(0.6.seconds) {
                        showPopup = false
                        timeout = null
                    }
                }
            }
            div {
                className = CssClasses.popupContainer
                span {
                    className = CssClasses.popup
                    +"text copied"
                    hidden = !showPopup
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

suspend fun encodeUsername(username: String) =
    sha256(username)
