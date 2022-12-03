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

import com.mypasswordgen.common.routes.CookieRoute
import com.mypasswordgen.jsclient.CssClasses
import com.mypasswordgen.jsclient.openIndexedDB
import com.mypasswordgen.jsclient.registerServiceWorker
import com.mypasswordgen.jsclient.resourcesFormat
import io.ktor.resources.*
import kotlinx.browser.localStorage
import react.FC
import react.Props
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
                    +("Cookies and related technologies allow us to provide a better user " +
                            "experience for you.")
                }
                p {
                    +("In particular we use cookies and related technologies to provide all the online mode functionality, and to " +
                            "remember your choices of background color, use mode and cookie preferences. Read more in our ")
                    a {
                        href = href(resourcesFormat, CookieRoute.Policy())
                        +"Cookie Policy."
                    }
                }
                p {
                    +"By clicking on a button you consent to the respective use of cookies/local storage."
                }
                p {
                    +"You can revoke your consent at any time visiting the "
                    a {
                        href = href(resourcesFormat, CookieRoute.OptOut())
                        +"Opt-Out Page."
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
                        localStorage.setItem("importExportType", ImportExportType.FILE.code)
                        registerServiceWorker()
                        openIndexedDB()
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
