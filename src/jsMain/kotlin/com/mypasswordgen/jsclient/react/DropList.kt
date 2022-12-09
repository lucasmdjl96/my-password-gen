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
import kotlinx.coroutines.MainScope
import org.w3c.dom.HTMLElement
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
private val mainPopup: HTMLElement?
    get() = getHtmlElementById("mainPopup")

external interface DropListProps : Props {
    var doOnChange: (String) -> Unit
    var set: Set<String>
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
                if (inputValue != "") props.doOnRemove(inputValue)
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
                if (!event.target.value.contains('%')) {
                    inputValue = event.target.value
                    props.doOnChange(event.target.value)
                } else {
                    mainPopup?.dispatchEvent(popupEvent(
                        "${props.name.replaceFirstChar(Char::uppercaseChar)} cannot contain tha character %",
                        PopupType.ERROR
                    ))
                }
            }
            onKeyDown = { event ->
                props.doOnEnter(event)
            }
        }
        datalist {
            id = "${props.name}List"
            for (item in props.set) {
                option {
                    key = item
                    value = item
                }
            }
        }
        button {
            className = CssClasses.addButton
            id = "${props.name}Add"
            disabled = props.disableAdd || inputValue == "" || inputValue.contains('%')
            +"Add"
            onClick = {
                if (inputValue != "" && !inputValue.contains('%')) props.doOnAdd(inputValue)
                ::focus on getHtmlElementById(props.name)
            }
        }
    }
}
