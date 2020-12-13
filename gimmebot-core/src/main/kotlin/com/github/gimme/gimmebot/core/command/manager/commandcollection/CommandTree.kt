package com.github.gimme.gimmebot.core.command.manager.commandcollection

import com.github.gimme.gimmebot.core.command.Command

/**
 * Represents a tree structure of commands.
 */
class CommandTree : CommandCollection {

    private val root: Node = Node("", null)

    override fun addCommand(command: Command) {
        val words = command.name.split(" ")

        var currentNode = root
        for (word in words) {
            currentNode = currentNode.children.computeIfAbsent(word) { k -> Node(k, null) }
        }
        currentNode.command = command
    }

    override fun getCommand(input: String): Command? {
        val words = input.split(" ")

        var lastFoundCommand: Command? = null

        var currentNode = root
        for (word in words) {
            currentNode = currentNode.children[word] ?: break
            lastFoundCommand = currentNode.command
        }
        return lastFoundCommand
    }

    override fun getCommands(): List<Command> {
        val list = mutableListOf<Command>()
        root.fetchCommands(list)
        return list
    }

    private data class Node(
        val name: String,
        var command: Command?,
    ) {
        val children: MutableMap<String, Node> = mutableMapOf()

        fun fetchCommands(list: MutableList<Command>) {
            command?.let { list.add(it) }
            children.values.forEach { child -> child.fetchCommands(list) }
        }
    }
}