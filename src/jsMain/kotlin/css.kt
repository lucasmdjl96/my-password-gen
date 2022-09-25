import csstype.*
import emotion.css.ClassName
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input

val background = ClassName {
    display = Display.flex
    justifyContent = JustifyContent.center
    alignItems = AlignItems.center
    height = 100.vh
    background = rgb(0, 0, 139)
}

val container = ClassName {
    display = Display.flex
    flexDirection = FlexDirection.column
    justifyContent = JustifyContent.spaceAround
    gap = 1.0.rem
    borderRadius = 1.0.rem
    alignItems = AlignItems.center
    width = 45.0.rem
    background = rgba(129, 124, 114, 0.5)
}

val inputContainer = ClassName {
    display = Display.flex
    flexDirection = FlexDirection.row
    justifyContent = JustifyContent.spaceAround
    borderRadius = 0.5.rem
    alignItems = AlignItems.center
    paddingTop = 2.0.rem
    paddingLeft = 2.0.rem
    paddingRight = 2.0.rem
    paddingBottom = 0.0.rem
    marginTop = 0.0.rem
    marginLeft = 2.0.rem
    marginRight = 2.0.rem
    marginBottom = 0.0.rem
    input {
        width = 100.pct
        borderRadius = 0.4.rem
        padding = 1.5.rem
        margin = 0.0.rem
        border = 0.px
        outline = 0.px
        fontSize = 1.5.rem
    }
    button {
        padding = 1.6.rem
        margin = 0.0.rem
        borderRadius = 0.3.rem
        border = 0.px
        fontSize = 1.5.rem
        textTransform = TextTransform.uppercase
    }
}

val addButton = ClassName {
    backgroundColor = rgb(144, 238, 144)
    color = rgb(0, 100, 0)
    hover {
        backgroundColor = rgb(124, 252, 0)
    }
    active {
        backgroundColor = rgb(100, 202, 0)
    }
    disabled {
        backgroundColor = rgb(143, 188, 143)
    }
}

val remButton = ClassName {
    backgroundColor = rgb(255, 123, 123)
    color = rgb(167, 0, 0)
    hover {
        backgroundColor = rgb(255, 0, 0)
    }
    active {
        backgroundColor = rgb(255, 50, 50)
    }
    disabled {
        backgroundColor = rgb(255, 186, 186)
    }
}

val titleClass = ClassName {
    fontSize = 3.0.rem
    color = rgb(250, 250, 250)
    paddingTop = 1.5.rem
    paddingLeft = 2.0.rem
    paddingRight = 2.0.rem
    paddingBottom = 0.5.rem
}

val buttonContainer = ClassName {
    display = Display.flex
    margin = 2.0.rem
    width = 80.pct
    justifyContent = JustifyContent.spaceAround
    gap = 2.0.rem
    button {
        padding = 1.0.rem
        width = 100.pct
        color = rgb(250, 250, 250)
        backgroundColor = rgb(0, 206, 209)
        textTransform = TextTransform.uppercase
        fontWeight = FontWeight.bold
        border = 0.px
        borderRadius = 0.3.rem
        hover {
            backgroundColor = rgb(30, 144, 255)
        }
        active {
            backgroundColor = rgb(24, 106, 205)
        }
        disabled {
            backgroundColor = rgb(173, 216, 230)
        }
    }
}

val passwordClass = ClassName {
    fontSize = 2.0.rem
    //color = NamedColor.darkred
    padding = 2.0.rem
    marginBottom = 2.0.rem
    //backgroundColor = rgba(250, 250, 250, 0.9)
    display = Display.flex
    alignItems = AlignItems.center
    gap = 1.0.rem
    border = 3.px
    borderStyle = LineStyle.solid
    borderColor = rgb(100, 100, 100)
    borderRadius = 1.0.rem
    backgroundColor = rgba(10, 10, 10, 0.9)
    color = rgb(255, 140, 0)
    width = 80.pct
    wordBreak = WordBreak.breakWord
    button {
        border = 0.px
        borderRadius = 0.5.rem
        height = 2.5.rem
        backgroundColor = rgba(0, 0, 0, 0.0)
        hover {
            backgroundColor = rgba(60, 60, 60, 0.5)
        }
        active {
            backgroundColor = rgba(36, 36, 36, 0.5)
        }
    }
}

val logOut = ClassName {
    color = rgb(139, 0, 0)
    fontSize = 15.px
    border = 0.px
    backgroundColor = rgba(0, 0, 0, 0.0)
    hover {
        color = rgb(255, 0, 0)
        fontWeight = FontWeight.bolder
    }
}