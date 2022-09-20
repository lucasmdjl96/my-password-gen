package com.lucasmdjl.application

import com.lucasmdjl.application.tables.Emails
import com.lucasmdjl.application.tables.Sites
import com.lucasmdjl.application.tables.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init() {
        val driverClassName = "org.postgresql.Driver"
        val jdbcUrl = "jdbc:postgresql://localhost:5432/"
        val database = Database.connect(url = jdbcUrl, driver = driverClassName, user = "postgres")
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users, Emails, Sites)
        }
    }

}