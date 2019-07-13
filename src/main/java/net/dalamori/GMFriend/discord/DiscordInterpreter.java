package net.dalamori.GMFriend.discord;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class DiscordInterpreter extends ListenerAdapter {

    AbstractCommand interpreter;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        CommandContext context = interpret(event.getMessage().getContentRaw(), event.getAuthor().getAsTag());

        if (context.getResponse() != null) {
            event.getChannel().sendMessage(context.getResponse()).queue();
        }
    }

    public CommandContext interpret(String rawCommand, String owner) {
        CommandContext context = new CommandContext();

        context.setOwner(owner);

        context.setCommand(Arrays.asList(rawCommand.split("\\s")));

        context.setIndex(0);
        try {
            interpreter.handle(context);

        } catch (InterpreterException ex) {
            context.setResponse("Error Received, and not caught!: " + ex.getMessage());
            log.debug("DiscordInterpreter::interpret got an error parsing the command: {}",
                    rawCommand, ex);
        }

        return context;
    }
}
