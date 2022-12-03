@file:JsModule("react-qr-reader")
@file:JsNonModule
@file:Suppress("INTERFACE_WITH_SUPERCLASS", "OVERRIDING_FINAL_MEMBER", "RETURN_TYPE_MISMATCH_ON_OVERRIDE", "CONFLICTING_OVERLOADS")

package com.mypasswordgen.jsclient

import org.w3c.dom.mediacapture.MediaTrackConstraints
import react.FC
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

external interface UseQrReaderHookProps {
    var constraints: MediaTrackConstraints?
        get() = definedExternally
        set(value) = definedExternally
    var onResult: ((result: Result?, error: Error?) -> Unit)?
        get() = definedExternally
        set(value) = definedExternally
    var scanDelay: Number?
        get() = definedExternally
        set(value) = definedExternally
    var videoId: String?
        get() = definedExternally
        set(value) = definedExternally
}

external var useQrReader: (UseQrReaderHookProps) -> Unit

external val QrReader: FC<QrReaderProps>
