package com.github.gimme.gimmebot.discord.command.mediums

import com.github.gimme.gimmebot.core.command.manager.CommandManager
import com.github.gimme.gimmebot.core.command.medium.TextCommandMedium
import com.github.gimme.gimmebot.discord.command.ChannelCommandSender
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

/**
 * Accepts input from Discord channels.
 *
 * @param T the command manager's response type
 */
class ChannelCommandMedium<T>(
    private val jda: JDA,
    commandPrefix: String?,
    commandManager: CommandManager<T>,
    converter: (T) -> String?,
    includeConsoleListener: Boolean = true,
) : TextCommandMedium<T>(commandManager, converter, includeConsoleListener, commandPrefix) {

    override fun onInstall() {
        jda.addEventListener(object : ListenerAdapter() {
            override fun onMessageReceived(event: MessageReceivedEvent) {
                val sender = ChannelCommandSender(event.channel, event.author)
                val message = event.message.contentRaw

                parseInput(sender, message)
            }
        })
    }
}