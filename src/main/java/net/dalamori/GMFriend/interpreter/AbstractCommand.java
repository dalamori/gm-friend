package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.exceptions.InterpreterException;

public abstract class AbstractCommand {

    public abstract void handle(CommandContext context) throws InterpreterException;

    public String getCurrentCommandPart(CommandContext context, int offset) {
        String cmdPart = "";
        int requestedIndex = context.getIndex() + offset;
        if (requestedIndex < context.getCommand().size()) {
            cmdPart = context.getCommand().get(requestedIndex);
        }

        return cmdPart;
    }

    public String getCurrentCommandPart(CommandContext context) {
        return getCurrentCommandPart(context, 0);
    }

    public String getRemainingCommand(CommandContext context, int offset) {
        String remaining = "";
        int fromIndex = context.getIndex() + 1 + offset;

        if (fromIndex < context.getCommand().size()) {
            remaining = String.join(" ",
                    context.getCommand().subList(fromIndex, context.getCommand().size())
            );
        }

        return remaining;
    }

    public String getRemainingCommand(CommandContext context) {
        return getRemainingCommand(context, 0);
    }
}

