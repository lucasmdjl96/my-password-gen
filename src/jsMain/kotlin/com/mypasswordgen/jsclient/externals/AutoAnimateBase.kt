/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

@file:JsModule("@formkit/auto-animate")
@file:JsNonModule

package com.mypasswordgen.jsclient.externals

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
