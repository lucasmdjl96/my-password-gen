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

import com.mypasswordgen.jsclient.CssClasses
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
