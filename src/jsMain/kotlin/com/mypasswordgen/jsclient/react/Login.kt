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
import com.mypasswordgen.jsclient.dto.LoginDto
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.AutoComplete
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.useState

external interface LoginProps : Props {
    var onLogin: (LoginDto) -> Unit
    var onRegister: (LoginDto) -> Unit
}

val Login = FC<LoginProps> { props ->
    var username by useState("")
    var password by useState("")


    div {
        className = CssClasses.inputContainer
        input {
            id = "username"
            placeholder = "Username"
            ariaLabel = "Username"
            autoComplete = AutoComplete.off
            type = InputType.text
            value = username
            autoFocus = true
            onChange = {
                username = it.target.value
            }
            onKeyDown = withReceiver {
                if (!ctrlKey && key == "Enter") {
                    ::focus on getHtmlElementById("password")!!
                } else if (ctrlKey && key == "ArrowDown") {
                    ::focus on getHtmlElementById("password")!!
                }
            }
        }
    }
    div {
        className = CssClasses.inputContainer
        Password {
            this.password = password
            this.onChange = {
                password = it
            }
        }
    }
    div {
        className = CssClasses.buttonContainer
        button {
            +"Log in"
            id = "login"
            onClick = {
                if (username != "" && password != "") props.onLogin(LoginDto(username, password))
            }
        }
        button {
            +"Register"
            id = "register"
            onClick = {
                if (username != "" && password != "") props.onRegister(LoginDto(username, password))
            }
        }
    }

}
