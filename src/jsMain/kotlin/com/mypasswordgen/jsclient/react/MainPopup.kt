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
import com.mypasswordgen.jsclient.externals.autoAnimateRefCallBack
import kotlinx.js.timers.clearTimeout
import kotlinx.js.timers.setTimeout
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.CustomEvent
import org.w3c.dom.events.CustomEventInit
import react.*
import react.dom.html.ReactHTML.div
import kotlin.time.Duration.Companion.seconds

external interface MainPopupProps : Props

val MainPopup = FC<MainPopupProps> {
    val parent = useRef<HTMLDivElement>(null)
    var message by useState<String?>(null)
    var type by useState<PopupType>()
    useEffect(parent.current) {
        if (parent.current != null) parent.current!!.addEventListener("popup", { event ->
            val customEvent = event.unsafeCast<CustomEvent<PopupInfo>>()
            message = customEvent.detail.message
            type = customEvent.detail.type
        })
    }
    rawUseEffect(dependencies = Dependencies(1) { message }, effect = {
        val timeout = if (message != null) setTimeout(2.0.seconds) {
            message = null
        } else null
        return@rawUseEffect {
            if (timeout != null) clearTimeout(timeout)
        }
    })
    div {
        ref = parent
        id = "mainPopup"
        className = CssClasses.mainPopupContainer
        div {
            className = CssClasses.mainPopupSubContainer
            ref = autoAnimateRefCallBack()
            if (message != null) div {
                className =
                    if (type == PopupType.ERROR) CssClasses.errorPopup
                    else if (type == PopupType.SUCCESS) CssClasses.successPopup
                    else null
                +message!!
            }
        }
    }
}

enum class PopupType {
    ERROR, SUCCESS;
}

data class PopupInfo(val message: String, val type: PopupType)

fun popupEvent(message: String, type: PopupType) = CustomEvent("popup", object:
    CustomEventInit<PopupInfo> {
    override var detail = PopupInfo(message, type)
})
