package react

import CssClasses
import clipboard
import crypto.sha256
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.clearTimeout
import kotlinx.js.timers.setTimeout
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span
import kotlin.time.Duration.Companion.seconds

private val scope = MainScope()

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
