package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import com.lucasmdjl.passwordgenerator.jsclient.hsl
import com.lucasmdjl.passwordgenerator.jsclient.updateSession
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
}

val OnlineToggle = FC<OnlineToggleProps> { props ->
    div {
        className = CssClasses.onlineToggle
        div {
            +"Offline"
            onClick = {
                if (props.cookiesAccepted == true) {
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
                    if (props.cookiesAccepted == true) {
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
                if (props.cookiesAccepted == true) {
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
