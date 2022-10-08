package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.span

external interface TitleProps : Props {
    var loggedIn: Boolean
    var online: Boolean
    var reset: () -> Unit
}

val TitleContainer = FC<TitleProps> { props ->
    div {
        className = CssClasses.titleContainer
        h1 {
            className = CssClasses.title
            +"${if (props.loggedIn && !props.online) "Offline " else ""}Password Generator"
        }
        if (props.loggedIn) {
            button {
                className = CssClasses.logOut
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
}
