package com.mypasswordgen.jsclient.react

import com.mypasswordgen.jsclient.CssClasses
import kotlinx.browser.localStorage
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.InputType
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.input

external interface ColorPickerProps : Props {
    var background: String
    var cookiesAccepted: Boolean?
    var updateBackground: (String) -> Unit
}

val ColorPicker = FC<ColorPickerProps> { props ->
    div {
        className = CssClasses.colorPickerContainer
        input {
            className = CssClasses.colorPicker
            id = "backgroundColor"
            ariaLabel = "Background Color"
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
