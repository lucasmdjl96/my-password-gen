package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import kotlinx.browser.document
import org.w3c.dom.HTMLElement
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
        onKeyDown = { event ->
            if (!event.ctrlKey && event.key == "Enter") {
                val buttonId = if (event.altKey) "register" else "login"
                (document.getElementById(buttonId)!! as HTMLElement).click()
            } else if (event.ctrlKey && event.key == "s") {
                event.preventDefault()
                (document.getElementById("showPassword")!! as HTMLElement).click()
            } else if (event.ctrlKey && event.key == "ArrowUp") {
                (document.getElementById("username")!! as HTMLElement).focus()
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
