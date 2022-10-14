package com.lucasmdjl.passwordgenerator.server.test.mapper

import com.lucasmdjl.passwordgenerator.server.test.TestParent
import io.mockk.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class MapperTestParent : TestParent() {

    fun mockTransaction() {
        val slot = slot<Transaction.() -> Any>()
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = capture(slot)) } answers { slot.invoke(mockk()) }
    }

}
