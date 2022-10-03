package react

import CssClasses
import react.dom.html.InputType
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.span

external interface PasswordProps : Props {
    var password: String
    var onChange: (String) -> Unit
}

val Password = FC<PasswordProps> { props ->
    var visible by useState(false)
    input {
        id = "password"
        placeholder = "Password"
        type = if (visible) InputType.text else InputType.password
        value = props.password
        onChange = {
            props.onChange(it.target.value)
        }
    }
    span {
        className = CssClasses.materialIcon
        +if (visible) "visibility_off" else "visibility"
        onClick = {
            visible = !visible
        }
    }
}
