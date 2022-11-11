package com.mypasswordgen.server.plugins

import com.mypasswordgen.server.tables.Emails
import com.mypasswordgen.server.tables.Sessions
import com.mypasswordgen.server.tables.Sites
import com.mypasswordgen.server.tables.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.datetime.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.dateLiteral
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun Application.initDatabase() {
    pluginLogger.debug { "Installing Database" }
    val jdbcUrlBase = environment.config.property("postgres.jdbcUrlBase").getString()
    val jdbcUrlHost = environment.config.property("postgres.host").getString()
    val jdbcUrlPort = environment.config.property("postgres.port").getString()
    val jdbcUrlDatabase = environment.config.property("postgres.database").getString()
    val hikariConfig = HikariConfig().apply {
        driverClassName = environment.config.property("postgres.driver").getString()
        jdbcUrl = "$jdbcUrlBase://$jdbcUrlHost:$jdbcUrlPort/$jdbcUrlDatabase"
        username = environment.config.property("postgres.username").getString()
        password = environment.config.property("postgres.password").getString()
    }
    val dataSource = HikariDataSource(hikariConfig)
    val database = Database.connect(dataSource)
    transaction(database) {
        addLogger(StdOutSqlLogger)
        SchemaUtils.createMissingTablesAndColumns(Sessions, Users, Emails, Sites)
        Sessions.deleteWhere {
            Sessions.dateCreated less dateLiteral(
                Clock.System.todayAt(TimeZone.UTC) - DatePeriod(days = 92)
            )
        }
        val scriptPath = environment.config.propertyOrNull("postgres.script")?.getString()
        if (scriptPath != null) {
            val script = File(scriptPath)
            if (script.exists() && script.isFile && script.canRead()) exec(script.readText())
        }
    }
}
