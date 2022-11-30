/*
 * This file is part of MyPasswordGen.
 *
 * MyPasswordGen is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * MyPasswordGen is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MyPasswordGen. If not, see <https://www.gnu.org/licenses/>.
 */

package com.mypasswordgen.jsclient.dto

import com.mypasswordgen.common.dto.fullServer.FullEmailServerDto
import com.mypasswordgen.common.dto.fullServer.FullSessionServerDto
import com.mypasswordgen.common.dto.fullServer.FullSiteServerDto
import com.mypasswordgen.common.dto.fullServer.FullUserServerDto
import kotlinx.serialization.SerializationException

object UglySerializer {

    private fun String.escape(char: Char, vararg chars: Char): String {
        var result = this.replace("$char", "\\$char")
        for (otherChar in chars) {
            result = result.replace("$otherChar", "\\$otherChar")
        }
        return result
    }

    private fun String.deEscape(char: Char, vararg chars: Char): String {
        var result = this.replace("\\$char", "$char")
        for (otherChar in chars) {
            result = result.replace("\\$otherChar", "$otherChar")
        }
        return result
    }

    private fun String.escape() = this.escape('[', ']', ',')
    private fun String.deEscape() = this.deEscape('[', ']', ',')

    private fun serialize(fullSiteServerDto: FullSiteServerDto): String {
        return fullSiteServerDto.siteName.escape()
    }

    private fun serialize(fullEmailServerDto: FullEmailServerDto): String {
        return fullEmailServerDto.emailAddress.escape() + fullEmailServerDto.sites.joinToString(",", "[", "]") {
            serialize(it)
        }
    }

    private fun serialize(fullUserServerDto: FullUserServerDto): String {
        return fullUserServerDto.username.escape() + fullUserServerDto.emails.joinToString("", "[", "]") {
            serialize(it)
        }.replace("[]", ",").replace(",]", "]")
    }

    private fun serialize(fullSessionServerDto: FullSessionServerDto): String {
        return fullSessionServerDto.users.joinToString("", "[", "]") {
            serialize(it)
        }.replace("[]", ",").replace(",]", "]")
    }

    fun serialize(downloadSession: DownloadSession): String {
        return downloadSession.downloadCode.short + serialize(downloadSession.data)
    }

    fun serialize(downloadUser: DownloadUser): String {
        return "${downloadUser.downloadCode.short}{${serialize(downloadUser.data)}}"
    }

    fun deserializeUser(downloadUser: String): DownloadUser {
        if (!downloadUser.startsWith("${DownloadCode.USER.short}{")
            || !downloadUser.endsWith("}")) throw SerializationException()
        val str = downloadUser.substringBetween("${DownloadCode.USER.short}{", "}")
        val tree = deserializeBase(str).groupBy { node -> node.parent }
        if (tree[null]?.size != 1) throw SerializationException()
        val user = tree[null]!![0]
        return DownloadUser(fullUserFrom(user, tree))
    }

    fun deserializeSession(downloadSession: String): DownloadSession {
        if (!downloadSession.startsWith("${DownloadCode.SESSION.short}[")
            || !downloadSession.endsWith("]")) throw SerializationException()
        val str = downloadSession.substringBetween("${DownloadCode.SESSION.short}[", "]")
        val tree = deserializeBase(str).groupBy { node -> node.parent }
        return DownloadSession(FullSessionServerDto {
            if (tree[null] != null) {
                for (user in tree[null]!!) {
                    +fullUserFrom(user, tree)
                }
            }
        })
    }

    private fun fullUserFrom(user: Node, tree: Map<Node?, List<Node>>) = FullUserServerDto(user.value) {
        if (tree[user] != null) {
            for (email in tree[user]!!) {
                +FullEmailServerDto(email.value) {
                    if (tree[email] != null) {
                        for (site in tree[email]!!) {
                            +FullSiteServerDto(site.value)
                        }
                    }
                }
            }
        }

    }

    private fun deserializeBase(str: String): List<Node> {
        var depth = 0
        var currentValue = StringBuilder()
        val nodes = mutableListOf<Node>()
        var parent: Node? = null
        var previousChar: Char? = null
        for (char in str) {
            if (previousChar == '\\') {
                currentValue.append(char)
            } else when (char) {
                '[' -> {
                    depth++
                    val node = Node(parent, currentValue.toString().deEscape())
                    if (node.value == "") throw SerializationException()
                    nodes.add(node)
                    currentValue = StringBuilder()
                    parent = node
                }

                ']' -> {
                    depth--
                    val node = Node(parent, currentValue.toString().deEscape())
                    if (node.value != "") {
                        nodes.add(node)
                    }
                    currentValue = StringBuilder()
                    parent = parent!!.parent
                }

                ',' -> {
                    val node = Node(parent, currentValue.toString().deEscape())
                    if (node.value == "") throw SerializationException()
                    nodes.add(node)
                    currentValue = StringBuilder()
                }

                else -> {
                    currentValue.append(char)
                }
            }
            previousChar = char
        }
        if (depth != 0) throw SerializationException()
        val leftOver = currentValue.toString()
        if (leftOver != "") {
            val node = Node(parent, leftOver.deEscape())
            nodes.add(node)
        }
        return nodes
    }

}

private class Node(val parent: Node?, val value: String) {
    override fun toString(): String {
        return "${parent?.value} <- $value"
    }
}

private fun String.substringBetween(start: String, end: String) =
    this.substringAfter(start).substringBeforeLast(end)
