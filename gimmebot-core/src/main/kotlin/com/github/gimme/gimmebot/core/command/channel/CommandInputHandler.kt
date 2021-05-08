package com.github.gimme.gimmebot.core.command.channel

import com.github.gimme.gimmebot.core.command.CommandSender

/**
 * Can handle command input.
 */
interface CommandInputHandler {

    /** Parses the specified [input] as the given [sender] and returns if the [input] matched a command. */
    fun parseInput(sender: CommandSender, input: String): Boolean
}