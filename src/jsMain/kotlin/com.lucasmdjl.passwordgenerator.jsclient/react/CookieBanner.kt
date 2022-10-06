package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface CookieBannerProps : Props {
    var background: String
    var updateCookie: (Boolean) -> Unit
}

val CookieBanner = FC<CookieBannerProps> { props ->
    ReactHTML.div {
        className = CssClasses.cookieBanner
        ReactHTML.div {
            className = CssClasses.cookieContainer
            ReactHTML.div {
                +"Accept the cookies!!! (or else...)"
            }
            ReactHTML.div {
                className = CssClasses.cookieButtonContainer
                ReactHTML.button {
                    +"Accept"
                    onClick = {
                        props.updateCookie(true)
                        localStorage.setItem("cookiesAccepted", "true")
                        localStorage.setItem("backgroundColor", props.background)
                        localStorage.setItem("online", "false")
                    }
                }
                ReactHTML.button {
                    +"Decline"
                    onClick = {
                        props.updateCookie(false)
                    }
                }
            }
        }
    }
}
