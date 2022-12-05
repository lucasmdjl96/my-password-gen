@file:JsModule("@formkit/auto-animate/react")
@file:JsNonModule

package com.mypasswordgen.jsclient

import org.w3c.dom.Element

external fun <T : Element> useAutoAnimate(autoAnimateOptionsPartial: AutoAnimateOptionsPartial = definedExternally): AutoAnimateInstance<T>
