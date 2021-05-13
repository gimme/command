package com.github.gimme.gimmebot.core.command.manager

import com.github.gimme.gimmebot.core.command.Command
import com.github.gimme.gimmebot.core.command.exception.CommandException
import com.github.gimme.gimmebot.core.command.exception.ErrorCode
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandCollection
import com.github.gimme.gimmebot.core.command.manager.commandcollection.CommandMap
import com.github.gimme.gimmebot.core.command.sender.CommandSender


/**
 * Represents a command manager with base functionality.
 *
 * @param R the response type
 */
open class SimpleCommandManager<R>(private val defaultResponseParser: (Any?) -> R) : CommandManager<R> {

    private val executorByCommand: MutableMap<Command<*>, CommandNode<*, R>> = mutableMapOf()

    override val commandCollection: CommandCollection = CommandMap()

    final override fun <T> registerCommand(command: Command<T>, responseConverter: ((T) -> R)?) {
        commandCollection.addCommand(command)

        if (responseConverter != null) {
            executorByCommand[command] = CommandNode(command, responseConverter)
        }
    }

    override fun getCommand(name: String): Command<*>? = commandCollection.getCommand(name)

    override fun hasCommand(name: String): Boolean = commandCollection.containsCommand(name)

    override fun executeCommand(
        commandSender: CommandSender,
        commandName: String,
        arguments: List<String>,
    ): R {
        val command = getCommand(commandName) ?: throw ErrorCode.NOT_A_COMMAND.createException()
        val commandNode = executorByCommand[command]

        return commandNode?.execute(commandSender, arguments)
            ?: defaultResponseParser(command.execute(commandSender, arguments))
    }

    /**
     * Represents a wrapped [command] that can be executed with its response converted to a specific type, [R], through
     * the specified [responseParser]
     *
     * @param T the response type of the command
     * @param R the converted response type
     */
    private data class CommandNode<T, R>(
        /** The wrapped command. */
        val command: Command<T>,
        /** The response converter. */
        val responseParser: (T) -> R,
    ) {
        /**
         * Executes the wrapped [command] converting the response through the optional [responseParser] or else the
         * [defaultResponseParser].
         *
         * @throws CommandException if the command execution was unsuccessful
         */
        @Throws(CommandException::class)
        fun execute(commandSender: CommandSender, args: List<String>): R {
            val response = command.execute(commandSender, args)
            return responseParser(response)
        }
    }
}
