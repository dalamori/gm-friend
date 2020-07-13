package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.models.enums.UserRole;

import java.util.*;

@Data
@Slf4j
@EqualsAndHashCode(callSuper=false)
public class MapCommand extends AbstractCommand {

    private Map<String, MapSubcommand> map = new HashMap<>();
    private Set<String> helpKeys = new HashSet<>();
    private AbstractCommand defaultAction;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        String commandPart = getCurrentCommandPart(context).toLowerCase();

        // try to follow map
        if (map.containsKey(commandPart)) {
            UserRole roleRequired = map.get(commandPart).getRequiredRole();
            if (context.getRole().compareTo(roleRequired) < 0) {
                throw new InterpreterException("MapCommand::handle - Permission denied");
            }

            context.setIndex(context.getIndex() + 1);
            map.get(commandPart).getCommand().handle(context);
            return;
        }

        // otherwise, fall back to default
        tryDefault(context);
    }

    public AbstractCommand getAutoHelpCommand() {
        return new AbstractCommand() {
            @Override
            public void handle(CommandContext context) throws InterpreterException {
                context.setResponse(autoHelp(context));
            }
        };
    }

    public String autoHelp(CommandContext context) {
        String commandPrefix = String.join(" ", context.getCommand().subList(0, context.getIndex()));
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, MapSubcommand>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, MapSubcommand> entry = iterator.next();

            if (!this.helpKeys.contains(entry.getKey())) {
                continue;
            }

            if (entry.getValue().getRequiredRole() != null && context.getRole().compareTo(entry.getValue().getRequiredRole()) < 0) {
                continue;
            }

            builder.append(commandPrefix);
            builder.append(" ");
            builder.append(entry.getKey());
            builder.append(" - ");
            builder.append(entry.getValue().getHelpHint());
            builder.append("\n");
        }

        return builder.toString();
    }

    private void tryDefault(CommandContext context) throws InterpreterException {
        if (defaultAction == null) {
            log.error("MapCommand::handle Reached null default Action for command {} at index {}", context.getCommand(), context.getIndex());
            throw new InterpreterException("Default Action not set!");
        }

        defaultAction.handle(context);
    }

}
