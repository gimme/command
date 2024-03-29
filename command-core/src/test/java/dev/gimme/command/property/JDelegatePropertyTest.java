package dev.gimme.command.property;

import dev.gimme.command.BaseCommand;
import dev.gimme.command.UtilsKt;
import dev.gimme.command.parameter.CommandParameter;
import dev.gimme.command.permission.Permission;
import dev.gimme.command.sender.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JDelegatePropertyTest {

    static List<String> listInput = List.of("a", "b", "a");
    static Set<String> setInput = Set.of("a", "b");

    @Test
    void converts_delegate_properties_to_parameters_that_take_arguments() {

        var command = new DelegateTestCommand();
        Map<CommandParameter, Object> input = Map.of(
                requireNonNull(command.getParameters().get("string")), "a",
                requireNonNull(command.getParameters().get("i")), 1,
                requireNonNull(command.getParameters().get("d")), 0.5,
                requireNonNull(command.getParameters().get("list")), listInput,
                requireNonNull(command.getParameters().get("set")), setInput,
                requireNonNull(command.getParameters().get("collection")), listInput,
                requireNonNull(command.getParameters().get("iterable")), listInput
        );

        assertFalse(command.called[0]);
        command.execute(UtilsKt.getDUMMY_COMMAND_SENDER(), input);
        assertTrue(command.called[0]);
    }
}

class DelegateTestCommand extends BaseCommand<Void> {

    final boolean[] called = {false};

    private final Param<String> string = param()
            .defaultValue("a")
            .build();

    private final Param<Integer> i = param()
            .suggestions(HashSet::new)
            .build();

    private final Param<Double> d = param()
            .defaultValue(2d)
            .build();

    private final Param<Boolean> b = param()
            .defaultValue(true)
            .build();

    private final Param<List<String>> list = param()
            .defaultValue(new ArrayList<>())
            .build();

    private final Param<Set<String>> set = param().build();

    private final Param<? extends Collection<String>> collection = param().build();

    private final Param<? extends Iterable<String>> iterable = param().build();

    DelegateTestCommand() {
        super("test-command");
    }

    @Override
    public Void call() {
        called[0] = true;

        var listInput = JDelegatePropertyTest.listInput;
        var setInput = JDelegatePropertyTest.setInput;

        assertEquals("a", string.get());
        assertEquals(1, i.get());
        assertEquals(0.5, d.get());
        assertEquals(true, b.get());
        assertIterableEquals(listInput, list.get());
        assertIterableEquals(setInput, set.get());
        assertIterableEquals(listInput, collection.get());
        assertIterableEquals(listInput, iterable.get());

        return null;
    }
}

class Sender1 implements CommandSender {
    @NotNull
    @Override
    public String getName() {
        return "sender1";
    }

    @Override
    public void sendMessage(@NotNull String message) {
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return true;
    }
}

class Sender2 implements CommandSender {
    @NotNull
    @Override
    public String getName() {
        return "sender2";
    }

    @Override
    public void sendMessage(@NotNull String message) {
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return true;
    }
}

class PlayerSender implements CommandSender {

    Player player = new Player();

    @NotNull
    @Override
    public String getName() {
        return "sender2";
    }

    @Override
    public void sendMessage(@NotNull String message) {
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return true;
    }
}

class Player {}
