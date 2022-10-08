package com.lucasmdjl.passwordgenerator.jsclient.react

import com.lucasmdjl.passwordgenerator.jsclient.CssClasses
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label

external interface ColorPickerProps : Props {
    var background: String
    var cookiesAccepted: Boolean?
    var updateBackground: (String) -> Unit
}

val ColorPicker = FC<ColorPickerProps> { props ->
    div {
        className = CssClasses.colorPickerContainer
        label {
            +"Background Color"
            htmlFor = "backgroundColor"
            hidden = true
        }
        input {
            className = CssClasses.colorPicker
            id = "backgroundColor"
            type = InputType.color
            value = props.background
            onChange = { event ->
                val color = event.target.value
                props.updateBackground(color)
                if (props.cookiesAccepted == true) localStorage.setItem("backgroundColor", color)
            }
        }
    }
}
