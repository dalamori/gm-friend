package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.InterpreterException;

public abstract class AbstractCommand {

    public abstract void handle(CommandContext context) throws InterpreterException;

    public String getCurrentCommandPart(CommandContext context) {
        String cmdPart = "";
        if (context.getIndex() < context.getCommand().size()) {
            cmdPart = context.getCommand().get(context.getIndex());
        }

        return cmdPart.toLowerCase();
    }

    public String getRemainingCommand(CommandContext context) {
        String remaining = "";
        int fromIndex = context.getIndex() + 1;

        if (fromIndex < context.getCommand().size()) {
            remaining = String.join(" ",
                    context.getCommand().subList(fromIndex, context.getCommand().size())
            );
        }

        return remaining;
    }
}

