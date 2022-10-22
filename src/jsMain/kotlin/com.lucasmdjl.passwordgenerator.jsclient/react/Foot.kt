package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.common.routes.AboutRoute
import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import com.lucasmdjl.passwordgenerator.jsclient.resourcesFormat
import io.ktor.resources.*
import react.FC
import react.Props
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div

val Foot = FC<Props> {
    div {
        className = CssClasses.foot
        a {
            href = href(resourcesFormat, AboutRoute())
            +"About"
        }
        a {
            href = href(resourcesFormat, CookieRoute.Policy())
            +"Privacy policy"
        }
    }
}
