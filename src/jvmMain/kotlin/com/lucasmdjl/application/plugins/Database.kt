package com.lucasmdjl.application.plugins

import com.lucasmdjl.application.tables.Emails
import com.lucasmdjl.application.tables.Sessions
import com.lucasmdjl.application.tables.Sites
import com.lucasmdjl.application.tables.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.initDatabase() {
    val hikariConfig = HikariConfig().apply {
        driverClassName = environment.config.property("postgres.driver").getString()
        jdbcUrl = environment.config.property("postgres.jdbcUrl").getString()
        username = environment.config.property("postgres.username").getString()
        password = environment.config.property("postgres.password").getString()
    }
    val dataSource = HikariDataSource(hikariConfig)
    val database = Database.connect(dataSource)
    transaction(database) {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Sessions, Users, Emails, Sites)
    }
}
