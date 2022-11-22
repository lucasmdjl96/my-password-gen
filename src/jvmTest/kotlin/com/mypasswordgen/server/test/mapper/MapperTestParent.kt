/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.mapper

import com.mypasswordgen.server.test.TestParent
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
