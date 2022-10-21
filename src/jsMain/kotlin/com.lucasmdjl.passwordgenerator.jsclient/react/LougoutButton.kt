package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.span

external interface LogoutButtonProps : Props {
    var reset: () -> Unit
}

val LogoutButton = FC<LogoutButtonProps> { props ->
    div {
        className = CssClasses.logoutContainer
        button {
            className = CssClasses.logOut
            id = "logout"
            span {
                className = CssClasses.materialIcon
                +"logout"
            }
            onClick = {
                props.reset()
            }
        }
    }
}
