package com.mypasswordgen.jsclient.react

import com.mypasswordgen.common.routes.AboutRoute
import com.mypasswordgen.common.routes.CookieRoute
import com.mypasswordgen.jsclient.CssClasses
import com.mypasswordgen.jsclient.resourcesFormat
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
