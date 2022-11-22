package com.mypasswordgen.jsclient.react

import com.mypasswordgen.jsclient.CssClasses
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.clearTimeout
import kotlinx.js.timers.setTimeout
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import kotlin.time.Duration.Companion.seconds

external interface MainPopupProps : Props {
    var showErrorPopup: Boolean
    var changeErrorPopup: (Boolean) -> Unit
    var showSuccessPopup: Boolean
    var changeSuccessPopup: (Boolean) -> Unit
}

val MainPopup = FC<MainPopupProps> { props ->
    div {
        className = CssClasses.mainPopupContainer
        div {
            id = "errorPopup"
            hidden = !props.showErrorPopup
            className = CssClasses.errorPopup
            var timeout: Timeout? = null
            onClick = {
                props.changeErrorPopup(true)
                if (timeout != null) clearTimeout(timeout!!)
                timeout = setTimeout(2.0.seconds) {
                    props.changeErrorPopup(false)
                    timeout = null
                }
            }
        }
        div {
            id = "successPopup"
            hidden = !props.showSuccessPopup
            className = CssClasses.successPopup
            var timeout: Timeout? = null
            onClick = {
                props.changeSuccessPopup(true)
                if (timeout != null) clearTimeout(timeout!!)
                timeout = setTimeout(2.0.seconds) {
                    props.changeSuccessPopup(false)
                    timeout = null
                }
            }
        }
    }
}
