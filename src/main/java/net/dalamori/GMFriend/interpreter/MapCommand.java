package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.InterpreterException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@EqualsAndHashCode(callSuper=false)
public class MapCommand extends AbstractCommand {

    private Map<String, AbstractCommand> map = new HashMap<>();
    private AbstractCommand defaultAction;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        List<String> command = context.getCommand();
        int atIndex = context.getIndex();

        // make sure we have something to interpret
        if (atIndex >= command.size()) {
            tryDefault(context);
        }

        // try to follow map
        String commandPart = command.get(atIndex);
        if (map.containsKey(commandPart)) {
            context.setIndex(atIndex + 1);
            map.get(commandPart).handle(context);

            return;
        }

        // otherwise, fall back to default
        tryDefault(context);
    }

    private void tryDefault(CommandContext context) throws InterpreterException {
        if (defaultAction == null) {
            log.error("MapCommand::handle Reached null default Action for command {} at index {}", context.getCommand(), context.getIndex());
            throw new InterpreterException("Default Action not set!");
        }

        defaultAction.handle(context);
    }

}
