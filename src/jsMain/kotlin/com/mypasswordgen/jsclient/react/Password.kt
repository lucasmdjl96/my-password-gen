package com.mypasswordgen.jsclient.react

import com.mypasswordgen.jsclient.CssClasses
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.InputType
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span
import react.useState

external interface PasswordProps : Props {
    var password: String
    var onChange: (String) -> Unit
}

val Password = FC<PasswordProps> { props ->
    var visible by useState(false)
    input {
        id = "password"
        placeholder = "Password"
        ariaLabel = "Password"
        type = if (visible) InputType.text else InputType.password
        value = props.password
        onChange = {
            props.onChange(it.target.value)
        }
        onKeyDown = withReceiver {
            if (!ctrlKey && key == "Enter") {
                val buttonId = if (altKey) "register" else "login"
                ::click on getHtmlElementById(buttonId)!!
            } else if (ctrlKey && key == "s") {
                preventDefault()
                ::click on getHtmlElementById("showPassword")!!
            } else if (ctrlKey && key == "ArrowUp") {
                ::focus on getHtmlElementById("username")!!
            }
        }
    }
    span {
        className = CssClasses.materialIcon
        +if (visible) "visibility_off" else "visibility"
        id = "showPassword"
        onClick = {
            visible = !visible
        }
    }
}
