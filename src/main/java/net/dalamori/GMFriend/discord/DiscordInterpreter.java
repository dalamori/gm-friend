package net.dalamori.GMFriend.discord;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class DiscordInterpreter extends ListenerAdapter {

    private static final int MAX_DISCORD_MESSAGE_LENGTH = 1995;  // actually 2000, but I want a little room at the end.

    AbstractCommand interpreter;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        CommandContext context = interpret(event.getMessage().getContentRaw(), event.getAuthor().getAsTag());

        if (context.getResponse() != null) {
            String output = context.getResponse();
            if (output.length() > MAX_DISCORD_MESSAGE_LENGTH) {
                output = StringUtils.abbreviate(output, "...", MAX_DISCORD_MESSAGE_LENGTH);
            }

            event.getChannel().sendMessage(output).queue();
        }
    }

    public CommandContext interpret(String rawCommand, String owner) {
        CommandContext context = new CommandContext();

        context.setOwner(owner);

        context.setCommand(Arrays.asList(rawCommand.split("\\s+")));

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
