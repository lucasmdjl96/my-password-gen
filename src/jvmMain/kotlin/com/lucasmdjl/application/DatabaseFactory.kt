package com.lucasmdjl.application

import com.lucasmdjl.application.tables.Emails
import com.lucasmdjl.application.tables.Sites
import com.lucasmdjl.application.tables.Users
import io.ktor.server.config.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(config: ApplicationConfig) {
        val driverClassName = config.property("postgres.driver").getString()
        val jdbcUrl = config.property("postgres.jdbcUrl").getString()
        val user = config.property("postgres.username").getString()
        val password = config.property("postgres.password").getString()
        val database = Database.connect(url = jdbcUrl, driver = driverClassName, user = user, password = password)
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users, Emails, Sites)
        }
    }

}