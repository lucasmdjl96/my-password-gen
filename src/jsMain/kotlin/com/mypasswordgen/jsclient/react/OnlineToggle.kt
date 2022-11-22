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
import com.mypasswordgen.jsclient.hsl
import com.mypasswordgen.jsclient.updateSession
import emotion.react.css
import kotlinx.browser.localStorage
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

private val toggleOnColor = hsl(200, 100, 45)
private val toggleOffColor = hsl(0, 0, 50)

external interface OnlineToggleProps : Props {
    var cookiesAccepted: Boolean?
    var online: Boolean
    var updateOnline: (Boolean) -> Unit
    var connectionOn: Boolean
}

val OnlineToggle = FC<OnlineToggleProps> { props ->
    div {
        className = CssClasses.onlineToggle
        div {
            +"Offline"
            onClick = {
                if (props.cookiesAccepted == true && props.connectionOn) {
                    props.updateOnline(false)
                    localStorage.setItem("online", "false")
                }
            }
        }
        div {
            css(CssClasses.toggleContainer) {
                backgroundColor = if (props.online) toggleOnColor else toggleOffColor
            }
            span {
                className = CssClasses.materialIconOutlined
                +if (props.online) "toggle_on" else "toggle_off"
                id = "onlineToggle"
                onClick = {
                    if (props.cookiesAccepted == true && props.connectionOn) {
                        val newOnline = !props.online
                        props.updateOnline(newOnline)
                        localStorage.setItem("online", "$newOnline")
                        if (newOnline) scope.launch {
                            updateSession()
                        }
                    }
                }
            }
        }
        div {
            +"Online"
            onClick = {
                if (props.cookiesAccepted == true && props.connectionOn) {
                    props.updateOnline(true)
                    localStorage.setItem("online", "true")
                    scope.launch {
                        updateSession()
                    }
                }
            }
        }
    }
}
