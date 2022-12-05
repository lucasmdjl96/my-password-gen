/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.jsclient

import csstype.ClassName
import csstype.Color

object CssClasses {
    val background = ClassName("background")

    val container = ClassName("container")

    val inputContainer = ClassName("inputContainer")

    val addButton = ClassName("addButton")

    val removeButton = ClassName("remButton")

    val title = ClassName("titleClass")

    val buttonContainer = ClassName("buttonContainer")

    val password = ClassName("passwordClass")

    val logOut = ClassName("logOut")

    val logoutContainer = ClassName("logoutContainer")

    val materialIcon = ClassName("material-icons")

    val materialIconOutlined = ClassName("material-icons-outlined")

    val titleContainer = ClassName("titleContainer")

    val popup = ClassName("popup")

    val popupContainer = ClassName("popupContainer")

    val onlineToggle = ClassName("onlineToggle")

    val toggleContainer = ClassName("toggleContainer")

    val colorPickerContainer = ClassName("colorPickerContainer")

    val colorPicker = ClassName("colorPicker")

    val fileManagerContainer = ClassName("fileManagerContainer")

    val cookieBanner = ClassName("cookieBanner")

    val cookieContainer = ClassName("cookieContainer")

    val cookieText = ClassName("cookieText")

    val cookieButtonContainer = ClassName("cookieButtonContainer")

    val foot = ClassName("foot")

    val donate = ClassName("donate")

    val errorPopup = ClassName("errorPopup")

    val successPopup = ClassName("successPopup")

    val mainPopupContainer = ClassName("mainPopupContainer")
    val mainPopupSubContainer = ClassName("mainPopupSubContainer")

    val qrContainerOuter = ClassName("qrContainerOuter")
    val qrContainerMid = ClassName("qrContainerMid")
    val qrContainerInner = ClassName("qrContainerInner")
}

@Suppress("NOTHING_TO_INLINE")
inline fun hsl(hue: Int, saturation: Int, lightning: Int): Color =
    "hsl($hue, $saturation%, $lightning%)".unsafeCast<Color>()
