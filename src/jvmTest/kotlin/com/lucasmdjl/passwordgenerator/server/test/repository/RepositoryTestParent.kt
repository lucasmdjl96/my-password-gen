package com.lucasmdjl.passwordgenerator.server.test.repository

import com.lucasmdjl.passwordgenerator.server.tables.Emails
import com.lucasmdjl.passwordgenerator.server.tables.Sessions
import com.lucasmdjl.passwordgenerator.server.tables.Sites
import com.lucasmdjl.passwordgenerator.server.tables.Users
import com.lucasmdjl.passwordgenerator.server.test.TestParent
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class RepositoryTestParent : TestParent() {

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
            exec(this::class.java.classLoader.getResource("testDatabaseInit.sql")!!.readText())
        }
    }

    fun <T> testTransaction(block: Transaction.() -> T) = transaction(database) {
        block()
        rollback()
    }

    override fun initMocks() {}

    override fun initDummies() {}

}
