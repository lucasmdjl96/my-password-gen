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

import com.mypasswordgen.common.routes.AboutRoute
import com.mypasswordgen.common.routes.CookieRoute
import com.mypasswordgen.jsclient.CssClasses
import com.mypasswordgen.jsclient.resourcesFormat
import io.ktor.resources.*
import kotlinx.browser.window
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.input

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
    div {
        className = CssClasses.donate
        form {
            action = "https://www.paypal.com/donate"
            method = "post"
            target = "_top"
            input {
                type = InputType.hidden
                name = "hosted_button_id"
                value = "WCRYMKW6UAJRC"
            }
            input {
                type = InputType.image
                val size = if (window.innerWidth > 450) "LG" else "SM"
                src = "https://www.paypalobjects.com/en_US/i/btn/btn_donate_$size.gif"
                name = "submit"
                title = "PayPal - The safer, easier way to pay online!"
                alt = "Donate with PayPal button"
            }
        }
    }
}
