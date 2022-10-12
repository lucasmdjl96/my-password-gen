package com.lmdjl.passwordgenerator.server.service

import com.lmdjl.passwordgenerator.server.TestParent
import io.mockk.*
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

abstract class ServiceTestParent : TestParent() {

    fun mockTransaction() {
        val slot = slot<Transaction.() -> Any>()
        mockkStatic("org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManagerKt")
        every { transaction(statement = capture(slot)) } answers { slot.invoke(mockk()) }
    }

}
