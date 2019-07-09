package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.InterpreterException;

public abstract class AbstractCommand {

    public abstract void handle(CommandContext context) throws InterpreterException;

    public String getCurrentCommandPart(CommandContext context) {
        String cmdPart = null;
        if (context.getIndex() < context.getCommand().size()) {
            cmdPart = context.getCommand().get(context.getIndex());
        }

        return cmdPart;
    }

    public String getRemainingCommand(CommandContext context) {
        String remaining = "";

        if (context.getIndex() < context.getCommand().size()) {
            remaining = String.join(" ",
                    context.getCommand().subList(context.getIndex(), context.getCommand().size())
            );
        }

        return remaining;
    }
}

