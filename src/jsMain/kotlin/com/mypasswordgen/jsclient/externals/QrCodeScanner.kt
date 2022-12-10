/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

@file:JsModule("react-qr-reader")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.mypasswordgen.jsclient.externals

import org.w3c.dom.mediacapture.MediaTrackConstraints
import react.Props
import react.ReactElement

external interface Result {
    var text: String?
        get() = definedExternally
        set(value) = definedExternally
}

external interface QrReaderProps : Props {
    var constraints: MediaTrackConstraints
    var onResult: ((result: Result?, error: Throwable?) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var ViewFinder: ((props: Any) -> ReactElement<Props>?)?
        get() = definedExternally
        set(value) = definedExternally
    var scanDelay: Number?
        get() = definedExternally
        set(value) = definedExternally
    var videoId: String?
        get() = definedExternally
        set(value) = definedExternally
    var className: String?
        get() = definedExternally
        set(value) = definedExternally
    var containerStyle: Any?
        get() = definedExternally
        set(value) = definedExternally
    var videoContainerStyle: Any?
        get() = definedExternally
        set(value) = definedExternally
    var videoStyle: Any?
        get() = definedExternally
        set(value) = definedExternally
}
