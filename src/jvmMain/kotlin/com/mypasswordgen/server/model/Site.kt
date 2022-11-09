package com.mypasswordgen.server.model

import com.mypasswordgen.server.tables.Sites
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

class Site(id: EntityID<UUID>) : Entity<UUID>(id) {

    var name by Sites.siteName
    var email by Email referencedOn Sites.emailId

    override fun toString(): String {
        return "[Site#${id.value}: $name]"
    }

    companion object : EntityClass<UUID, Site>(Sites)

}
