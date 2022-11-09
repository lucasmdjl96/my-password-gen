package com.mypasswordgen.jsclient

import org.w3c.dom.Window
import org.w3c.dom.events.EventTarget

abstract external class VisualViewport : EventTarget {
    var height: Int
    var width: Int
}

inline val Window.visualViewport: VisualViewport
    get() = asDynamic().visualViewport as VisualViewport
