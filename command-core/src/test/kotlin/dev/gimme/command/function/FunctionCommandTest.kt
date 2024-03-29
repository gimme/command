package dev.gimme.command.function

import dev.gimme.command.BaseCommand
import dev.gimme.command.DUMMY_COMMAND_SENDER
import dev.gimme.command.annotations.CommandFunction
import dev.gimme.command.annotations.Default
import dev.gimme.command.annotations.Name
import dev.gimme.command.sender.SenderTypes
import dev.gimme.command.annotations.Parameter
import dev.gimme.command.sender.CommandSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FunctionCommandTest {

    private val sender = DUMMY_COMMAND_SENDER

    @Test
    fun `Calls function command with parameters`() {
        var called = false

        val arg1 = "abc"
        val arg2 = 123
        val arg3 = listOf("three", "four")
        val args = listOf<Any>(arg1, arg2, arg3)

        val command = object : BaseCommand<Any?>("k") {

            @CommandFunction
            private fun call(s: CommandSender, a: String, b: Int, c: List<String>) {
                called = true

                assertEquals(s, sender)
                assertEquals(arg1, a)
                assertEquals(arg2, b)
                assertEquals(arg3, c)
            }
        }

        assertFalse(called)
        command.execute(sender, args.mapIndexed { index, arg -> command.parameters.getAt(index) to arg }.toMap())
        assertTrue(called)

        assertNotNull(command.parameters["a"])
        assertNotNull(command.parameters["b"])
        assertNotNull(command.parameters["c"])
    }

    @Test
    fun `Uses default values`() {
        var called = false

        val arg1 = "abc"
        val args = listOf<Any>(arg1)

        val command = object : BaseCommand<Any?>("k") {

            @CommandFunction
            private fun call(
                @Parameter(def = Default("xyz"))
                a: String,
                @Parameter(def = Default("xyz"))
                b: String,
                @Parameter(def = Default("5"))
                c: Int,
            ) {
                called = true

                assertEquals(arg1, a)
                assertEquals("xyz", b)
                assertEquals(5, c)
            }
        }

        assertFalse(called)
        command.execute(sender, args.mapIndexed { index, arg -> command.parameters.getAt(index) to arg }.toMap())
        assertTrue(called)
    }

    @Test
    fun `Uses named parameters`() {
        var called = false

        val arg1 = "abc"
        val arg2 = "xyz"
        val args = listOf<Any>(arg1, arg2)

        val command = object : BaseCommand<Any?>("k") {

            @CommandFunction
            private fun call(
                @Parameter(value = "param1")
                a: String,
                @Name("p2")
                b: String,
            ) {
                called = true

                assertEquals(arg1, a)
                assertEquals(arg2, b)
            }
        }

        assertFalse(called)
        command.execute(sender, args.mapIndexed { index, arg -> command.parameters.getAt(index) to arg }.toMap())
        assertTrue(called)

        assertNotNull(command.parameters["param1"])
        assertNotNull(command.parameters["p2"])
    }

    @Test
    fun `Parses sender subtype`() {
        class PlayerSender : CommandSender {
            override val name = "player"
            override fun sendMessage(message: String) {
            }
        }

        val sender = PlayerSender()

        var called = false

        val command = object : BaseCommand<Any?>("k") {

            @CommandFunction
            private fun call(playerSender: PlayerSender, playerSender2: PlayerSender?) {
                called = true

                assertEquals(playerSender, sender)
                assertEquals(playerSender2, sender)
            }
        }

        assertFalse(called)
        command.execute(sender, mapOf())
        assertTrue(called)
    }

    @Test
    fun `Parses custom sender type`() {
        class PlayerSender : CommandSender {
            override val name = "player"
            override fun sendMessage(message: String) {
            }
        }

        class Player(val name: String)
        SenderTypes.registerAdapter { s: PlayerSender -> Player(s.name) }

        val sender = PlayerSender()

        var called = false

        val command = object : BaseCommand<Any?>("k") {

            @CommandFunction
            private fun call(@dev.gimme.command.annotations.Sender player: Player) {
                called = true

                assertEquals(player.name, sender.name)
            }
        }

        assertFalse(called)
        command.execute(sender, mapOf())
        assertTrue(called)
    }
}

/* TODO
@Suppress("UNUSED_PARAMETER")
class JFunctionCommandTest {

    @Test
    fun `should execute reflection command with all types`() {
        var called = false

        val command = object : BaseCommand<Any>("c") {
            @CommandExecutor
            fun c(
                string1: String,
                string2: String,
                int1: Int,
                int2: Int,
                double1: Double,
                double2: Double,
                boolean1: Boolean,
                boolean2: Boolean?,
            ) {
                assertAll(
                    { assertEquals("string", string1) },
                    { assertEquals("", string2) },
                    { assertEquals(1, int1) },
                    { assertEquals(-999, int2) },
                    { assertEquals(0.5, double1) },
                    { assertEquals(36.0, double2) },
                    { assertEquals(true, boolean1) },
                    { assertEquals(false, boolean2) },
                )

                called = true
            }
        }

        assertFalse(called)
        command.execute(DUMMY_COMMAND_SENDER, listOf("string", "", "1", "-999", "0.5", "36", "trUE", "false"))
        assertTrue(called)
    }

    @Test
    fun `command without return type should execute`() {
        var called = false

        val command = object : BaseCommand<Any>("c") {
            @CommandExecutor
            fun c(string1: String) {
                called = true
            }
        }

        assertFalse(called)
        command.execute(DUMMY_COMMAND_SENDER, listOf("abc"))
        assertTrue(called)
    }

    @ParameterizedTest
    @MethodSource("commandExecutor")
    fun `should execute reflection command`(
        args: String?,
        command: Command<String>,
        shouldExecute: Boolean = true,
    ) {
        val expected = DUMMY_RESPONSE

        try {
            val actual = command.execute(DUMMY_COMMAND_SENDER, args?.split(" ") ?: listOf())

            assertEquals(expected, actual, "Command was not executed when it should have been")
        } catch (e: CommandException) {
            assertTrue(!shouldExecute, "Command did not return with an error when it should have")
        }
    }

    @Test
    fun `should execute reflection command when using sender subtypes`() {
        val command = object : BaseCommand<String>("c") {
            @CommandExecutor
            fun c(sender: CommandSenderImpl): String {
                assertEquals(1, sender.getInt())
                return DUMMY_RESPONSE
            }
        }
        val commandSender: CommandSender = CommandSenderImpl()

        val expected = DUMMY_RESPONSE
        val actual = command.execute(commandSender, listOf())

        assertEquals(expected, actual, "Command was not executed when it should have been")
    }

    @ParameterizedTest
    @MethodSource("commandError")
    fun `should throw command exception`(
        args: String?,
        errorCode: ErrorCode?,
        sender: CommandSender,
    ) {
        val command = object : BaseCommand<String>("c") {
            @CommandExecutor
            fun c(sender: CommandSenderImpl, a: Int, b: Int? = null): String {
                assertEquals(1, sender.getInt())
                return DUMMY_RESPONSE
            }
        }

        val executeCommand = { command.execute(sender, args?.split(" ") ?: listOf()) }

        if (errorCode == null) {
            assertDoesNotThrow { executeCommand() }
            return
        }

        val exception = assertThrows<CommandException> { executeCommand() }
        assertEquals(errorCode.code, exception.code)
    }

    @Test
    fun `should get command usage`() {
        val command = object : BaseCommand<Any>("c") {
            @CommandExecutor("", "2")
            fun a(paramOne: Int, paramTwo: Int = 2) {
            }
        }

        assertEquals("c <param-one> [param-two=2]", command.usage)
    }

    @Test
    fun `should get command usage with command sender`() {
        val command = object : BaseCommand<Any>("c") {
            @CommandExecutor("", "2")
            fun a(sender: CommandSender, paramOne: Int, paramTwo: Int = 2) {
            }
        }

        assertEquals("c <param-one> [param-two=2]", command.usage)
    }

    companion object {
        @JvmStatic
        fun commandExecutor() = listOf(
            // BASIC TESTS
            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(): String = DUMMY_RESPONSE
                },
                true,
            ),

            // VARARG TESTS
            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg strings: String): String {
                        assertIterableEquals(listOf<String>(), strings.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "string1 string2 string3",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg strings: String): String {
                        assertIterableEquals(listOf("string1", "string2", "string3"), strings.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "a 1 2 3",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(string: String, vararg ints: Int): String {
                        assertEquals("a", string)
                        assertIterableEquals(listOf(1, 2, 3), ints.asIterable())
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "1",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg a: Double): String = DUMMY_RESPONSE
                },
                true,
            ),
            Arguments.of(
                "true",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg a: Boolean): String = DUMMY_RESPONSE
                },
                true,
            ),

            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: String): String = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a b",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: String): String = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int): String = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "1.0",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(vararg a: Int): String = DUMMY_RESPONSE
                },
                false,
            ),
            Arguments.of(
                "a",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Boolean): String = DUMMY_RESPONSE
                },
                false,
            ),

            // DEFAULTS TESTS
            Arguments.of(
                "a",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: String = "def"): String {
                        assertEquals("a", a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "a",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: String, b: String = "def"): String {
                        assertEquals("a", a)
                        assertEquals("def", b)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "1",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int = 0, b: Int = 3, c: Int = 44): String {
                        assertEquals(1, a)
                        assertEquals(3, b)
                        assertEquals(44, c)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int? = 4): String {
                        assertEquals(4, a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int? = null): String {
                        assertEquals(null, a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "abc",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(a: Int? = null): String = DUMMY_RESPONSE
                },
                false,
            ),

            // COMMAND SENDER TESTS
            Arguments.of(
                null,
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(sender: CommandSender): String {
                        assertEquals(DUMMY_COMMAND_SENDER, sender)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
            Arguments.of(
                "1",
                object : BaseCommand<String>("c") {
                    @CommandExecutor
                    fun c(sender: CommandSender, a: Int = 0): String {
                        assertEquals(DUMMY_COMMAND_SENDER, sender)
                        assertEquals(1, a)
                        return DUMMY_RESPONSE
                    }
                },
                true,
            ),
        )

        @JvmStatic
        fun commandError() = listOf(
            Arguments.of(
                "1",
                null,
                CommandSenderImpl(),
            ),
            Arguments.of(
                "a",
                ErrorCode.INVALID_ARGUMENT,
                CommandSenderImpl(),
            ),
            Arguments.of(
                "1 a",
                ErrorCode.INVALID_ARGUMENT,
                CommandSenderImpl(),
            ),
            Arguments.of(
                "1",
                ErrorCode.INCOMPATIBLE_SENDER,
                DUMMY_COMMAND_SENDER,
            ),
            Arguments.of(
                null,
                ErrorCode.TOO_FEW_ARGUMENTS,
                CommandSenderImpl(),
            ),
            Arguments.of(
                "1 2 3",
                ErrorCode.TOO_MANY_ARGUMENTS,
                CommandSenderImpl(),
            ),
        )
    }

    private class CommandSenderImpl : CommandSender {
        override val name: String
            get() = ""

        override fun sendMessage(message: String) {}

        fun getInt(): Int {
            return 1
        }
    }


    @Test
    fun `should get first command executor function`() {
        val commandExecutorFunction = object : BaseCommand<String>("c") {
            fun c1(): String {
                return ""
            }

            @CommandExecutor
            fun c2(): String {
                return ""
            }

            @CommandExecutor
            fun c3(): String {
                return ""
            }
        }.getFirstCommandExecutorFunction()

        assertEquals("c2", commandExecutorFunction.name)
    }

    @Test
    fun `getting non-existing command executor should throw exception`() {
        assertThrows<IllegalStateException> {
            object : BaseCommand<String>("c") {
                fun c(): String {
                    return ""
                }
            }.getFirstCommandExecutorFunction()
        }
    }

    @Test
    fun `should execute command`() {
        val command = object : BaseCommand<String>("c") {
            @CommandExecutor
            fun c(): String {
                return "abc"
            }
        }

        assertEquals("abc", tryExecuteCommandByReflection(command, DUMMY_COMMAND_SENDER, listOf()).toString())
    }

    @Test
    fun `executing command with wrong return type should throw exception`() {
        val command = object : BaseCommand<String>("c") {
            @CommandExecutor
            fun c(): Int {
                return 1
            }
        }

        assertThrows<ClassCastException> {
            tryExecuteCommandByReflection(command, DUMMY_COMMAND_SENDER, listOf()).toString()
        }
    }
}*/
