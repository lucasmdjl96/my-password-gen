import csstype.ClassName
import dto.Named
import kotlinx.coroutines.MainScope
import react.FC
import react.Props
import react.dom.html.AutoComplete
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.datalist
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.i
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.useState

private var scope = MainScope()

external interface DropListProps : Props {
    var doOnChange: (String) -> Unit
    var list: List<Named>
    var name: String
    var inputType: InputType
    var disableAdd: Boolean
    var doOnAdd: (String) -> Unit
    var doOnRemove: (String) -> Unit
}

val DropList = FC<DropListProps> { props ->
    var inputValue: String by useState("")

    div {
        className = CssClasses.inputContainer
        button {
            className = CssClasses.removeButton
            disabled = !props.disableAdd || inputValue == ""
            i {
                className = ClassName("fa-regular fa-trash-can")
            }
            onClick = {
                props.doOnRemove(inputValue)
                inputValue = ""
            }
        }
        label {
            +props.name
            htmlFor = props.name
            hidden = true
        }
        input {
            id = props.name
            type = props.inputType
            list = "${props.name}List"
            placeholder = props.name.replaceFirstChar { it.uppercaseChar() }
            autoComplete = AutoComplete.off
            value = inputValue
            onChange = { event ->
                inputValue = event.target.value
                props.doOnChange(event.target.value)
            }
        }
        datalist {
            id = "${props.name}List"
            for (item in props.list) {
                option {
                    key = item.name
                    value = item.name
                }
            }
        }
        button {
            className = CssClasses.addButton
            disabled = props.disableAdd || inputValue == ""
            +"Add"
            onClick = {
                props.doOnAdd(inputValue)
            }
        }
    }
}