package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.common.routes.CookieRoute
import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import com.lucasmdjl.passwordgenerator.jsclient.resourcesFormat
import io.ktor.resources.*
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.dom.html.AnchorTarget
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.p

external interface CookieBannerProps : Props {
    var background: String
    var updateCookie: (Boolean) -> Unit
    var dismiss: () -> Unit
}

val CookieBanner = FC<CookieBannerProps> { props ->
    div {
        className = CssClasses.cookieBanner
        div {
            className = CssClasses.cookieContainer
            div {
                className = CssClasses.cookieText
                p {
                    +("Cookies and using the local storage of your browser allow us to provide a better user " +
                            "experience for you.")
                }
                p {
                    +("In particular we use cookies to provide all the online mode functionality, and local storage to " +
                            "remember your choices of background color, use mode and cookie preferences. ")
                    a {
                        href = href(resourcesFormat, CookieRoute.Policy())
                        target = AnchorTarget._blank
                        +"Read more here"
                    }
                }
                p {
                    +"By clicking on a button you consent to the respective use of cookies/local storage."
                }
                p {
                    +"You can revoke your consent at any time visiting the "
                    a {
                        href = href(resourcesFormat, CookieRoute.OptOut())
                        target = AnchorTarget._blank
                        +"opt-out page"
                    }
                }
            }
            div {
                className = CssClasses.cookieButtonContainer
                button {
                    +"Accept All"
                    onClick = {
                        props.updateCookie(true)
                        props.dismiss()
                        localStorage.setItem("cookiesAccepted", "true")
                        localStorage.setItem("backgroundColor", props.background)
                        localStorage.setItem("online", "false")
                    }
                }
                button {
                    +"Reject All"
                    onClick = {
                        props.dismiss()
                    }
                }
                button {
                    +"Reject, but remember my choice"
                    onClick = {
                        props.updateCookie(false)
                        localStorage.setItem("cookiesAccepted", "false")
                        props.dismiss()
                    }
                }
            }
        }
    }
}
