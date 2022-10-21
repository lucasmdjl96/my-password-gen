package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import kotlinx.coroutines.MainScope
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.dom.events.KeyboardEvent
import react.dom.html.AutoComplete
import react.dom.html.InputType
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.datalist
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.option
import react.dom.html.ReactHTML.span
import react.useState

var scope = MainScope()

external interface DropListProps : Props {
    var doOnChange: (String) -> Unit
    var list: List<String>
    var name: String
    var inputType: InputType
    var disableAdd: Boolean
    var doOnAdd: (String) -> Unit
    var doOnRemove: (String) -> Unit
    var doOnEnter: (KeyboardEvent<HTMLInputElement>) -> Unit
    var autoFocus: Boolean
}

val DropList = FC<DropListProps> { props ->
    var inputValue: String by useState("")

    div {
        className = CssClasses.inputContainer
        button {
            className = CssClasses.removeButton
            id = "${props.name}Remove"
            disabled = !props.disableAdd || inputValue == ""
            span {
                className = CssClasses.materialIcon
                +"delete"
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
            autoFocus = props.autoFocus
            onChange = { event ->
                inputValue = event.target.value
                props.doOnChange(event.target.value)
            }
            onKeyDown = { event ->
                props.doOnEnter(event)
            }
        }
        datalist {
            id = "${props.name}List"
            for (item in props.list) {
                option {
                    key = item
                    value = item
                }
            }
        }
        button {
            className = CssClasses.addButton
            id = "${props.name}Add"
            disabled = props.disableAdd || inputValue == ""
            +"Add"
            onClick = {
                props.doOnAdd(inputValue)
            }
        }
    }
}
