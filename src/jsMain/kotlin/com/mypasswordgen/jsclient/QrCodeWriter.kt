/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */
@file:JsModule("qrcode.react")
@file:JsNonModule
package com.mypasswordgen.jsclient

import react.FC
import react.Props

external interface ImageSettings {
    var src: String
    var height: Int
    var width: Int
    var excavate: Boolean
    var x: Int?
        get() = definedExternally
        set(value) = definedExternally
    var y: Int?
        get() = definedExternally
        set(value) = definedExternally
}

external interface QRProps : Props {
    var value: String
    var size: Int?
        get() = definedExternally
        set(value) = definedExternally
    var level: String?
        get() = definedExternally
        set(value) = definedExternally
    var bgColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var fgColor: String?
        get() = definedExternally
        set(value) = definedExternally
    var style: Any?
        get() = definedExternally
        set(value) = definedExternally
    var includeMargin: Boolean?
        get() = definedExternally
        set(value) = definedExternally
    var imageSettings: ImageSettings?
        get() = definedExternally
        set(value) = definedExternally
}

external val QRCodeSVG: FC<QRProps>
external val QRCodeCanvas: FC<QRProps>
