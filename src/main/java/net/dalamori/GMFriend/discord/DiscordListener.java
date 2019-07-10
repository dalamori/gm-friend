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
public class DiscordListener extends ListenerAdapter {

    AbstractCommand interpreter;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        CommandContext context = new CommandContext();

        if (event.getAuthor().isBot()) {
            return;
        }

        context.setOwner(event.getAuthor().getAsTag());

        context.setCommand(Arrays.asList(event.getMessage().getContentRaw().split("\\s")));
        context.setIndex(0);

        try {
            interpreter.handle(context);

            if (context.getResponse() != null) {
                event.getChannel().sendMessage(context.getResponse()).queue();
            }

        } catch (InterpreterException ex) {
            event.getChannel().sendMessage("Error Received, and not caught!: " + ex.getMessage() ).queue();
            log.debug("DiscordListener::onMessageReceived got an error parsing the command: {}",
                    event.getMessage().getContentRaw(), ex);
        }

    }
}
