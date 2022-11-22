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
