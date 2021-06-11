package dev.gimme.gimmeapi.boot.command.executor

import dev.gimme.gimmeapi.boot.command.DUMMY_COMMAND_SENDER
import dev.gimme.gimmeapi.boot.command.FunctionCommand
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows

class CommandReflectionFunctionsTest {

    @Test
    fun `should get first command executor function`() {
        val commandExecutorFunction = object : FunctionCommand<String>("c") {
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
            object : FunctionCommand<String>("c") {
                fun c(): String {
                    return ""
                }
            }.getFirstCommandExecutorFunction()
        }
    }

    @Test
    fun `should execute command`() {
        val command = object : FunctionCommand<String>("c") {
            @CommandExecutor
            fun c(): String {
                return "abc"
            }
        }

        assertEquals("abc", tryExecuteCommandByReflection(command, DUMMY_COMMAND_SENDER, listOf()).toString())
    }

    @Test
    fun `executing command with wrong return type should throw exception`() {
        val command = object : FunctionCommand<String>("c") {
            @CommandExecutor
            fun c(): Int {
                return 1
            }
        }

        assertThrows<ClassCastException> {
            tryExecuteCommandByReflection(command, DUMMY_COMMAND_SENDER, listOf()).toString()
        }
    }

    @Test
    fun `camel case should be split into separate lowercase words`() {
        assertAll(
            { assertEquals("lorem ipsum", "loremIpsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "LoremIpsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "lorem ipsum".splitCamelCase(" ")) },
            { assertEquals("lorem ipsum", "Lorem Ipsum".splitCamelCase(" ")) },
            { assertEquals("lorem-ipsum", "loremIpsum".splitCamelCase("-")) },
            { assertEquals("lorem-ipsum", "lorem ipsum".splitCamelCase("-")) },
            { assertEquals("lorem-2", "lorem2".splitCamelCase("-")) },
            { assertEquals("lorem-2-ipsum", "lorem2Ipsum".splitCamelCase("-")) },
        )
    }
}