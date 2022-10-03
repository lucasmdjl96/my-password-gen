package com.lucasmdjl.passwordgenerator.server.model

import com.lucasmdjl.passwordgenerator.server.tables.Sites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Site(id: EntityID<Int>) : Entity<Int>(id) {

    var name by Sites.siteName
    var email by Email referencedOn Sites.emailId

    override fun toString(): String {
        return "[Site#${id.value}: $name]"
    }

    companion object : EntityClass<Int, Site>(Sites)

}