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
