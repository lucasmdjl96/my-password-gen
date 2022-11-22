/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.server.test.tables

import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
import com.mypasswordgen.server.test.TestParent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@Testcontainers
abstract class TablesTestParent : TestParent() {

    companion object {
        @Container
        val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14.5-alpine")
            .withDatabaseName("postgresTestDatabase")
            .withUsername("postgres")
            .withPassword("postgres")
    }

    private lateinit var database: Database

    @BeforeAll
    fun initDatabase() {
        database = Database.connect(postgres.jdbcUrl, postgres.driverClassName, postgres.username, postgres.password)
        transaction(database) {
            SchemaUtils.drop(Sessions, Users, Emails, Sites)
            SchemaUtils.create(Sessions, Users, Emails, Sites)
        }
    }

    fun <T> testTransaction(block: Transaction.() -> T) = transaction(database) {
        block()
        rollback()
    }

    fun exec(sql: String) = transaction(database) {
        exec(sql)
    }

    override fun initMocks() {}

    override fun initDummies() {}

}
