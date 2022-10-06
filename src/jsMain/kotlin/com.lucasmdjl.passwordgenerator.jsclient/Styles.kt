package com.lucasmdjl.passwordgenerator.jsclient

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

    val materialIcon = ClassName("material-icons")

    val materialIconOutlined = ClassName("material-icons-outlined")

    val titleContainer = ClassName("titleContainer")

    val popup = ClassName("popup")

    val popupContainer = ClassName("popupContainer")

    val onlineToggle = ClassName("onlineToggle")

    val toggleContainer = ClassName("toggleContainer")

    val colorPickerContainer = ClassName("colorPickerContainer")

    val colorPicker = ClassName("colorPicker")

    val cookieBanner = ClassName("cookieBanner")

    val cookieContainer = ClassName("cookieContainer")

    val cookieButtonContainer = ClassName("cookieButtonContainer")
}

@Suppress("NOTHING_TO_INLINE")
inline fun hsl(hue: Int, saturation: Int, lightning: Int): Color =
    "hsl($hue, $saturation%, $lightning%)".unsafeCast<Color>()
