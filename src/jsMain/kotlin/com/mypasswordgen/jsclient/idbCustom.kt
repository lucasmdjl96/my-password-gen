package com.mypasswordgen.jsclient

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.js.jso
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromDynamic
import kotlinx.serialization.json.encodeToDynamic
import org.w3c.dom.events.Event
import kotlin.js.Promise


inline fun IDBFactory.open(name: String, version: Int, block: IDBOpenIDBRequest.() -> Unit) =
    open(name, version).apply(block)

inline fun openIDB(name: String, version: Int, block: IDBOpenIDBRequest.() -> Unit) =
    window.indexedDB.open(name, version, block)

suspend inline fun openDatabase(name: String, version: Int, block: IDBOpenIDBRequest.() -> Unit): IDBDatabase {
    val request = openIDB(name, version, block)
    val promise = Promise { resolve, reject ->
        request.onSuccess {
            resolve(request.result as IDBDatabase)
        }
        request.onError {
            reject(request.error)
        }
    }
    return promise.await()
}

inline infix fun <T> ((T) -> Unit)?.combinedWith(crossinline block: (T) -> Unit): (T) -> Unit = {
    if (this != null) this(it)
    block(it)
}

inline fun IDBOpenIDBRequest.onBlocked(crossinline action: (IDBVersionChangeEvent) -> Unit) = apply {
    onBlocked = onBlocked combinedWith {
        action(it)
    }
}

inline fun IDBOpenIDBRequest.onUpgradeNeeded(crossinline action: (IDBVersionChangeEvent) -> Unit) = apply {
    onUpgradeNeeded = onUpgradeNeeded combinedWith {
        action(it)
    }
}

inline fun IDBRequest.onError(crossinline action: (Event) -> Unit) = apply {
    onError = onError combinedWith {
        action(it)
    }
}

inline fun IDBRequest.onSuccess(crossinline action: (Event) -> Unit) = apply {
    onSuccess = onSuccess combinedWith {
        action(it)
    }
}

suspend inline fun IDBTransaction.onSuccessOf(vararg idbRequest: IDBRequest, crossinline block: IDBTransaction.() -> Unit) {
    Promise.all(idbRequest.map { request ->
        Promise { resolve, reject ->
            request.onSuccess = request.onSuccess combinedWith {
                resolve(Unit)
            }
            request.onError = request.onError combinedWith {
                reject(DOMException())
            }
        }
    }.toTypedArray()).then {
        block()
    }
}

inline fun IDBTransaction.onComplete(crossinline action: (Event) -> Unit) = apply {
    onComplete = onComplete combinedWith {
        action(it)
    }
}

inline fun IDBTransaction.onError(crossinline action: (Event) -> Unit) = apply {
    onError = onError combinedWith {
        action(it)
    }
}

inline fun IDBTransaction.onAbort(crossinline action: (Event) -> Unit) = apply {
    onAbort = onAbort combinedWith {
        action(it)
    }
}

inline fun IDBDatabase.onError(crossinline action: (Event) -> Unit) = apply {
    onError = onError combinedWith {
        action(it)
    }
}

inline fun IDBDatabase.onAbort(crossinline action: (Event) -> Unit) = apply {
    onAbort = onAbort combinedWith {
        action(it)
    }
}

inline fun IDBDatabase.onVersionChange(crossinline action: (Event) -> Unit) = apply {
    onVersionChange = onVersionChange combinedWith {
        action(it)
    }
}

inline fun IDBDatabase.onClose(crossinline action: (Event) -> Unit) = apply {
    onClose = onClose combinedWith {
        action(it)
    }
}


interface NamedObjectStore {
    val storeName: String
}


inline fun IDBDatabase.objectStoreNames() = objectStoreNames.toList()

inline fun IDBDatabase.createObjectStore(name: String, keyPath: Array<String>, autoIncrement: Boolean? = null) =
    createObjectStore(name, jso {
        this.keyPath = keyPath
        if (autoIncrement != null) this.autoIncrement = autoIncrement
    })

inline fun IDBDatabase.createObjectStore(name: String, keyPath: String, autoIncrement: Boolean? = null) =
    createObjectStore(name, jso {
        this.keyPath = keyPath
        if (autoIncrement != null) this.autoIncrement = autoIncrement
    })

inline fun IDBDatabase.createObjectStore(store: NamedObjectStore, keyPath: String, autoIncrement: Boolean? = null) =
    createObjectStore(store.storeName, keyPath, autoIncrement)

inline fun IDBDatabase.createObjectStore(
    store: NamedObjectStore,
    keyPath: Array<String>,
    autoIncrement: Boolean? = null
) =
    createObjectStore(store.storeName, keyPath, autoIncrement)

inline fun IDBDatabase.createObjectStore(
    store: NamedObjectStore,
    keyPath: List<String>,
    autoIncrement: Boolean? = null
) =
    createObjectStore(store.storeName, keyPath.toTypedArray(), autoIncrement)

inline fun IDBDatabase.deleteObjectStore(store: NamedObjectStore) = deleteObjectStore(store.storeName)

inline fun IDBDatabase.close(store: NamedObjectStore) = close(store.storeName)

inline fun <reified T/* : StoreType<T>*/> IDBDatabase.createObjectStore(
    keyPath: String,
    autoIncrement: Boolean? = null
) =
    createObjectStore(T::class.simpleName!!, keyPath, autoIncrement)

inline fun <reified T/* : StoreType<T>*/> IDBDatabase.deleteObjectStore() = deleteObjectStore(T::class.simpleName!!)
inline fun <reified T/* : StoreType<T>*/> IDBDatabase.close() = close(T::class.simpleName!!)

enum class TransactionMode(val jsName: String) {
    READ_WRITE("readwrite"), READ_ONLY("readonly"), READ_WRITE_FLUSH("readwriteflush")
}

infix fun IDBDatabase.ifBlocking(action: (Event) -> Unit) = apply {
    onVersionChange { event ->
        action(event)
    }
}

inline fun IDBDatabase.transaction(storeName: String, mode: TransactionMode, block: IDBTransaction.() -> Unit = {}) =
    transaction(storeName, mode.jsName).apply(block)

inline fun IDBDatabase.transaction(
    storeNames: Array<String>,
    mode: TransactionMode,
    block: IDBTransaction.() -> Unit = {}
) =
    transaction(storeNames, mode.jsName).apply(block)

inline fun IDBDatabase.readWriteTransaction(storeName: String, block: IDBTransaction.() -> Unit = {}) =
    transaction(storeName, TransactionMode.READ_WRITE, block)

inline fun IDBDatabase.readWriteTransaction(storeNames: Array<String>, block: IDBTransaction.() -> Unit = {}) =
    transaction(storeNames, TransactionMode.READ_WRITE, block)

inline fun IDBDatabase.readWriteTransaction(storeNames: List<String>, block: IDBTransaction.() -> Unit = {}) =
    readWriteTransaction(storeNames.toTypedArray(), block)

inline fun IDBDatabase.readWriteTransaction(
    storeName: String,
    vararg storeNames: String,
    block: IDBTransaction.() -> Unit = {}
) =
    readWriteTransaction(arrayOf(storeName, *storeNames), block)

inline fun IDBDatabase.readWriteTransaction(store: NamedObjectStore, block: IDBTransaction.() -> Unit = {}) =
    readWriteTransaction(store.storeName, block)

inline fun IDBDatabase.readWriteTransaction(stores: Array<NamedObjectStore>, block: IDBTransaction.() -> Unit = {}) =
    readWriteTransaction(stores.map(NamedObjectStore::storeName), block)

inline fun IDBDatabase.readWriteTransaction(stores: List<NamedObjectStore>, block: IDBTransaction.() -> Unit = {}) =
    readWriteTransaction(stores.toTypedArray(), block)

inline fun IDBDatabase.readWriteTransaction(
    store: NamedObjectStore,
    vararg stores: NamedObjectStore,
    block: IDBTransaction.() -> Unit = {}
) =
    readWriteTransaction(arrayOf(store, *stores), block)

inline fun IDBDatabase.readTransaction(storeName: String, block: IDBTransaction.() -> Unit = {}) =
    transaction(storeName, TransactionMode.READ_WRITE, block)

inline fun IDBDatabase.readTransaction(storeNames: Array<String>, block: IDBTransaction.() -> Unit = {}) =
    transaction(storeNames, TransactionMode.READ_WRITE, block)

inline fun IDBDatabase.readTransaction(storeNames: List<String>, block: IDBTransaction.() -> Unit = {}) =
    readTransaction(storeNames.toTypedArray(), block)

inline fun IDBDatabase.readTransaction(
    storeName: String,
    vararg storeNames: String,
    block: IDBTransaction.() -> Unit = {}
) =
    readTransaction(arrayOf(storeName, *storeNames), block)

inline fun IDBDatabase.readTransaction(store: NamedObjectStore, block: IDBTransaction.() -> Unit = {}) =
    readTransaction(store.storeName, block)

inline fun IDBDatabase.readTransaction(stores: Array<NamedObjectStore>, block: IDBTransaction.() -> Unit = {}) =
    readTransaction(stores.map(NamedObjectStore::storeName), block)

inline fun IDBDatabase.readTransaction(stores: List<NamedObjectStore>, block: IDBTransaction.() -> Unit = {}) =
    readTransaction(stores.toTypedArray(), block)

inline fun IDBDatabase.readTransaction(
    store: NamedObjectStore,
    vararg stores: NamedObjectStore,
    block: IDBTransaction.() -> Unit = {}
) =
    readTransaction(arrayOf(store, *stores), block)

inline fun <reified T/* : StoreType<T>*/> IDBDatabase.readWriteTransaction(block: IDBTransaction.() -> Unit = {}) =
    readWriteTransaction(T::class.simpleName!!, block)

inline fun <reified T/* : StoreType<T>*/> IDBDatabase.readTransaction(block: IDBTransaction.() -> Unit = {}) =
    readTransaction(T::class.simpleName!!, block)

inline fun <reified T/* : StoreType<T>*/, reified S/* : StoreType<S>*/> IDBDatabase.biReadWriteTransaction(block: IDBTransaction.() -> Unit = {}) =
    readWriteTransaction(T::class.simpleName!!, S::class.simpleName!!).apply(block)

inline fun <reified T/* : StoreType<T>*/, reified S/* : StoreType<S>*/> IDBDatabase.biReadTransaction(block: IDBTransaction.() -> Unit = {}) =
    readTransaction(T::class.simpleName!!, S::class.simpleName!!).apply(block)

inline fun DOMStringList.toList() = buildList {
    var i = 0
    while (i < this@toList.length) {
        val item = this@toList.item(i)
        if (item != null) add(item)
        i++
    }
}

inline fun IDBTransaction.objectStoreNames() = objectStoreNames.toList()

suspend inline fun IDBTransaction.awaitCompletion() = Promise { resolve, reject ->
    onComplete {
        resolve(Unit)
    }
}.await()

inline fun IDBTransaction.objectStore(store: NamedObjectStore) = objectStore(store.storeName)
inline fun <reified T> IDBTransaction.objectStore() = objectStore(T::class.simpleName!!)

inline fun IDBObjectStore.get(key: Any, block: IDBRequest.() -> Unit = {}) =
    get(key).apply(block)

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> IDBObjectStore.get(
    key: Any,
    configBlock: IDBRequest.() -> Unit = {},
    crossinline thenBlock: (T?) -> Unit = {}
): Promise<T?> {
    val request = get(key, configBlock)
    return Promise { resolve, reject ->
        request.onSuccess {
            resolve(
                if (request.result == null) null
                else Json.decodeFromDynamic<T>(request.result)
            )

        }
        request.onError {
            reject(
                request.error
            )
        }
    }.then {
        thenBlock(it)
        return@then it
    }
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> IDBObjectStore.add(t: T) = add(Json.encodeToDynamic(t))

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> IDBObjectStore.put(t: T) = put(Json.encodeToDynamic(t))

inline fun <reified T> IDBTransaction.get(
    key: Any,
    configBlock: IDBRequest.() -> Unit = {},
    crossinline thenBlock: (T?) -> Unit = {}
) = objectStore<T>().get<T>(key, configBlock, thenBlock)

inline fun <reified T> IDBTransaction.add(t: T) = objectStore<T>().add<T>(t)
inline fun <reified T> IDBTransaction.put(t: T) = objectStore<T>().put<T>(t)
inline fun <reified T> IDBTransaction.delete(key: Any) = objectStore<T>().delete(key)

inline fun <reified T> IDBTransaction.clear() = objectStore<T>().clear()

inline val IDBVersionChangeEvent.database: IDBDatabase
    get() = (this.currentTarget as IDBRequest).result as IDBDatabase

fun versionChangeLog(versionLog: VersionChangeLog.() -> Unit): (IDBVersionChangeEvent) -> Unit = { event ->
    val map = buildVersionChangeLog(versionLog).getMap()
    for (i in map.keys.sorted()) {
        if (event.oldVersion < i && i <= event.newVersion) {
            (map[i]!!)(event.database, event)
        }
    }
}

fun IDBOpenIDBRequest.versionChangeLog(versionLog: VersionChangeLog.() -> Unit) {
    onUpgradeNeeded { event ->
        val map = buildVersionChangeLog(versionLog).getMap()
        for (i in map.keys.sorted()) {
            if (event.oldVersion < i && i <= event.newVersion) {
                (map[i]!!)(event.database, event)
            }
        }
    }
}

class VersionChangeLog(private val mutableMap: MutableMap<Int, IDBDatabase.(IDBVersionChangeEvent) -> Unit>) {
    fun version(key: Int, value: IDBDatabase.(IDBVersionChangeEvent) -> Unit) = mutableMap.put(key, value)
    fun getMap() = mutableMap.toMap()

}

inline fun buildVersionChangeLog(action: VersionChangeLog.() -> Unit): VersionChangeLog =
    VersionChangeLog(mutableMapOf()).apply(action)
