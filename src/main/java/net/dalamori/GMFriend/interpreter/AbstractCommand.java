package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.InterpreterException;

import java.util.Arrays;
import java.util.List;

public abstract class AbstractCommand {

    public abstract void handle(CommandContext context) throws InterpreterException;

    /* TODO: this is in the wrong place, find a better spot for it ... */
    public void interpret(String commandString, String requestor) throws InterpreterException {
        List<String> command = Arrays.asList(commandString.split("\\s+"));

        CommandContext context = new CommandContext();
        context.setCommand(command);
        context.setIndex(0);
        context.setOwner(requestor);

        handle(context);

    }

}
