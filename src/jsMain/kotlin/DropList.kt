import dto.Named
import kotlinx.coroutines.MainScope
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.datalist
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.key

private var scope = MainScope()

external interface DropListProps : Props {
    var doOnChange: (String) -> Unit
    var list: List<Named>
    var name: String
    var inputType: InputType
    var disableAdd: Boolean
    var doOnAdd: (String) -> Unit
}

val DropList = FC<DropListProps> { props ->
    var inputValue = ""

    div {
        className = inputContainer
        label {
            +props.name
            htmlFor = props.name
            hidden = true
        }
        input {
            id = props.name
            type = props.inputType
            list = "${props.name}List"
            placeholder = props.name
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
            disabled = props.disableAdd
            +"Add"
            onClick = {
                props.doOnAdd(inputValue)
            }
        }
    }
}