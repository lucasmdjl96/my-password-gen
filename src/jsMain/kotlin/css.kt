import csstype.*
import emotion.css.ClassName
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.input

val background = ClassName {
    display = Display.flex
    justifyContent = JustifyContent.center
    alignItems = AlignItems.center
    height = 100.vh
    background = NamedColor.darkblue
}

val container = ClassName {
    display = Display.flex
    flexDirection = FlexDirection.column
    justifyContent = JustifyContent.spaceAround
    gap = 10.px
    borderRadius = 10.px
    alignItems = AlignItems.center
    width = 450.px
    background = rgba(129, 124, 114, 0.5)
}

val inputContainer = ClassName {
    display = Display.flex
    flexDirection = FlexDirection.row
    justifyContent = JustifyContent.spaceAround
    borderRadius = 5.px
    alignItems = AlignItems.center
    paddingTop = 20.px
    paddingLeft = 20.px
    paddingRight = 20.px
    paddingBottom = 0.px
    marginTop = 0.px
    marginLeft = 20.px
    marginRight = 20.px
    marginBottom = 0.px
    input {
        width = 100.pct
        borderRadius = 0.25.rem
        padding = 1.rem
        margin = 0.px
        border = 0.px
        outline = 0.px
        fontSize = 15.px
    }
    button {
        padding = 16.px
        margin = 0.px
        borderRadius = 3.px
        backgroundColor = NamedColor.lightgreen
        border = 0.px
        fontSize = 15.px
        color = NamedColor.darkgreen
        hover {
            backgroundColor = NamedColor.lawngreen
        }
        disabled {
            backgroundColor = NamedColor.darkseagreen
        }
    }
}

val titleClass = ClassName {
    fontSize = 30.px
    color = rgb(250, 250, 250)
    paddingTop = 15.px
    paddingLeft = 20.px
    paddingRight = 20.px
    paddingBottom = 5.px
}

val buttonContainer = ClassName {
    display = Display.flex
    margin = 20.px
    width = 80.pct
    justifyContent = JustifyContent.spaceAround
    gap = 20.px
    button {
        padding = 10.px
        width = 100.pct
        color = rgb(250, 250, 250)
        backgroundColor = NamedColor.darkturquoise
        textTransform = TextTransform.uppercase
        fontWeight = FontWeight.bold
        border = 0.px
        borderRadius = 3.px
        hover {
            backgroundColor = NamedColor.dodgerblue
        }
        disabled {
            backgroundColor = NamedColor.lightblue
        }
    }
}

val passwordClass = ClassName {
    fontSize = 20.px
    color = NamedColor.darkred
    padding = 20.px
    backgroundColor = rgba(250, 250, 250, 0.9)
    display = Display.flex
    gap = 10.px
    button {
        border = 0.px
        backgroundColor = rgba(0, 0, 0, 0.0)
        hover {
            backgroundColor = NamedColor.lightgrey
        }
    }
}

val logOut = ClassName {
    color = NamedColor.darkred
    fontSize = 15.px
    border = 0.px
    backgroundColor = rgba(0, 0, 0, 0.0)
    hover {
        color = NamedColor.red
        fontWeight = FontWeight.bolder
    }
}