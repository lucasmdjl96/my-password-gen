package com.mypasswordgen.jsclient.react

import com.mypasswordgen.jsclient.CssClasses
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1

external interface TitleProps : Props {
    var loggedIn: Boolean
    var online: Boolean
}

val TitleContainer = FC<TitleProps> { props ->
    div {
        className = CssClasses.titleContainer
        h1 {
            className = CssClasses.title
            +"${if (props.loggedIn && !props.online) "Offline " else ""}Password Generator"
        }
    }
}
