@file:Suppress("unused")

package com.lucasmdjl.passwordgenerator.jsclient

import org.w3c.dom.Window
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget
import kotlin.js.Promise

inline val Window.indexedDB: IDBFactory
    get() = asDynamic().indexedDB as IDBFactory

abstract external class IDBFactory {
    fun open(name: String, version: Int = definedExternally): IDBOpenIDBRequest
    fun deleteDatabase(name: String): IDBOpenIDBRequest
    fun cmp(first: Any, second: Any): Int
    fun databases(): Promise<Array<dynamic>>
}

abstract external class IDBOpenIDBRequest : IDBRequest {
    @JsName("onblocked")
    var onBlocked: ((IDBVersionChangeEvent) -> Unit)? = definedExternally

    @JsName("onupgradeneeded")
    var onUpgradeNeeded: ((IDBVersionChangeEvent) -> Unit)? = definedExternally
}

abstract external class IDBDatabase : EventTarget {
    val name: String
    val version: Int
    val objectStoreNames: DOMStringList

    fun close(name: String)
    fun createObjectStore(name: String, options: dynamic = definedExternally): IDBObjectStore
    fun deleteObjectStore(name: String)
    fun transaction(
        storeNames: Array<String>,
        mode: String = definedExternally,
        options: dynamic = definedExternally
    ): IDBTransaction

    fun transaction(
        storeNames: String,
        mode: String = definedExternally,
        options: dynamic = definedExternally
    ): IDBTransaction

    @JsName("onclose")
    var onClose: ((Event) -> Unit)? = definedExternally

    @JsName("onversionchange")
    var onVersionChange: ((Event) -> Unit)? = definedExternally

    @JsName("onabort")
    var onAbort: ((Event) -> Unit)? = definedExternally

    @JsName("onerror")
    var onError: ((Event) -> Unit)? = definedExternally
}

abstract external class DOMStringList {
    val length: Int

    fun item(index: Int): String?
    fun contains(item: String): Boolean
}

abstract external class IDBTransaction : EventTarget {
    val db: IDBDatabase = definedExternally
    val durability: String = definedExternally
    val error: DOMException?
    val mode: String = definedExternally
    val objectStoreNames: DOMStringList

    fun abort()
    fun objectStore(name: String): IDBObjectStore
    fun commit()

    @JsName("onabort")
    var onAbort: (Event) -> Unit = definedExternally

    @JsName("oncomplete")
    var onComplete: (Event) -> Unit = definedExternally

    @JsName("onerror")
    var onError: (Event) -> Unit = definedExternally
}

external class DOMException(message: String = definedExternally, name: String = definedExternally) : Throwable {
    override val message: String = definedExternally
    val name: String = definedExternally
}

abstract external class IDBRequest : EventTarget {
    val error: DOMException = definedExternally
    val result: dynamic = definedExternally
    val source: IDBRequestSource? = definedExternally
    val readyState: String = definedExternally
    val transaction: IDBTransaction? = definedExternally

    @JsName("onerror")
    var onError: ((Event) -> Unit)? = definedExternally

    @JsName("onsuccess")
    var onSuccess: ((Event) -> Unit)? = definedExternally
}

sealed external interface IDBRequestSource

abstract external class IDBObjectStore : IDBRequestSource, CursorSource {
    val indexNames: DOMStringList = definedExternally
    val keyPath: Any? = definedExternally
    var name: String = definedExternally
    val transaction: IDBTransaction = definedExternally
    val autoIncrement: Boolean = definedExternally

    abstract class CreateIndexOptions {
        var unique: Boolean = definedExternally
        var multiEntry: Boolean = definedExternally
    }

    fun add(value: dynamic, key: Any = definedExternally): IDBRequest
    fun clear(): IDBRequest
    fun count(query: Any?): IDBRequest
    fun createIndex(
        indexName: String,
        keyPath: String,
        objectParameters: CreateIndexOptions = definedExternally
    ): IDBIndex

    fun createIndex(
        indexName: String,
        keyPath: Array<String>,
        objectParameters: CreateIndexOptions = definedExternally
    ): IDBIndex

    fun delete(key: Any): IDBRequest
    fun deleteIndex(indexName: String)
    fun get(key: Any): IDBRequest
    fun getKey(key: Any): IDBRequest
    fun getAll(query: Any? = definedExternally, count: Int = definedExternally): IDBRequest
    fun getAllKeys(query: Any? = definedExternally, count: Int = definedExternally): IDBRequest
    fun index(name: String): IDBIndex
    fun openCursor(query: Any? = definedExternally, direction: String = definedExternally): IDBRequest
    fun openKeyCursor(query: Any? = definedExternally, direction: String = definedExternally): IDBRequest
    fun put(item: dynamic, key: Any = definedExternally): IDBRequest
}

abstract external class IDBIndex : IDBRequestSource, CursorSource {
    var name: String = definedExternally
    val objectStore: IDBObjectStore = definedExternally
    val keyPath: Any? = definedExternally
    val multiEntry: Boolean = definedExternally
    val unique: Boolean = definedExternally

    fun count(key: Any = definedExternally): IDBRequest
    fun get(key: Any? = definedExternally): IDBRequest
    fun getKey(key: Any? = definedExternally): IDBRequest
    fun getAll(query: Any? = definedExternally, count: Int = definedExternally): IDBRequest
    fun getAllKeys(query: Any? = definedExternally, count: Int = definedExternally): IDBRequest
    fun openCursor(range: Any? = definedExternally, direction: String = definedExternally): IDBRequest
    fun openKeyCursor(range: Any? = definedExternally, direction: String = definedExternally): IDBRequest
}

abstract external class IDBCursor : IDBRequestSource {
    val source: CursorSource = definedExternally
    val direction: String = definedExternally
    val key: Any? = definedExternally
    val primaryKey: Any? = definedExternally
    val request: IDBRequest = definedExternally

    fun advance(count: Int)

    @JsName("continue")
    fun cont(key: Any = definedExternally)
    fun continuePrimaryKey(key: Any, primaryKey: Any)
    fun delete()
    fun update(value: dynamic)
}

sealed external interface CursorSource

abstract external class IDBCursorWithValue : IDBCursor {
    val value: Any = definedExternally
}

abstract external class IDBKeyRange {
    val lower: Any = definedExternally
    val upper: Any = definedExternally
    val lowerOpen: Boolean = definedExternally
    val upperOpen: Boolean = definedExternally

    fun includes(key: Any): Boolean

    companion object {
        fun bound(
            lower: Any,
            upper: Any,
            lowerOpen: Boolean = definedExternally,
            upperOpen: Boolean = definedExternally
        ): IDBKeyRange

        fun only(value: Any): IDBKeyRange
        fun lowerBound(lower: Any, open: Boolean = definedExternally): IDBKeyRange
        fun upperBound(upper: Any, open: Boolean = definedExternally): IDBKeyRange
    }
}

external class IDBVersionChangeEvent(type: String, options: dynamic = definedExternally) : Event {
    val oldVersion: Int = definedExternally
    val newVersion: Int = definedExternally

    override val target: IDBRequest?
}
