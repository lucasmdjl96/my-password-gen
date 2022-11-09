package com.mypasswordgen.server.plugins

import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.initDatabase() {
    pluginLogger.debug { "Installing Database" }
    val jdbcUrlBase = environment.config.property("postgres.jdbcUrlBase").getString()
    val jdbcUrlHost = environment.config.property("postgres.host").getString()
    val jdbcUrlPort = environment.config.property("postgres.port").getString()
    val hikariConfig = HikariConfig().apply {
        driverClassName = environment.config.property("postgres.driver").getString()
        jdbcUrl = "$jdbcUrlBase://$jdbcUrlHost:$jdbcUrlPort/"
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
