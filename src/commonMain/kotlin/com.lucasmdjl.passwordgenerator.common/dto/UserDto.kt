package com.lucasmdjl.passwordgenerator.common.dto

import kotlinx.serialization.Serializable

@Serializable
class UserDto(val username: String, val emailList: MutableList<String>) {

    fun hasEmail(emailAddress: String): Boolean = emailList.find { it == emailAddress } != null

    fun addEmail(emailAddress: String) = emailList.add(emailAddress)

    fun removeEmail(emailAddress: String) = emailList.remove(emailAddress)

    override fun toString(): String {
        return "[UserDto: $username]"
    }

}





