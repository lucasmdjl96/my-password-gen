@file:JsModule("@formkit/auto-animate")
@file:JsNonModule

package com.mypasswordgen.jsclient

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement


external interface AnimationController {
    var parent: Element
    var enable: () -> Unit
    var disable: () -> Unit
    var isEnabled: () -> Boolean
}

external interface AutoAnimateOptionsPartial {
    var duration: Number?
        get() = definedExternally
        set(value) = definedExternally
    var easing: String? /* "linear" | "ease-in" | "ease-out" | "ease-in-out" | String? */
        get() = definedExternally
        set(value) = definedExternally
    var disrespectUserMotionPreference: Boolean?
        get() = definedExternally
        set(value) = definedExternally
}

@JsName("default")
external fun autoAnimate(el: HTMLElement, config: AutoAnimateOptionsPartial = definedExternally): AnimationController
