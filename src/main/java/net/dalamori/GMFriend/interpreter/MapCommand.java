package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.InterpreterException;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
@EqualsAndHashCode(callSuper=false)
public class MapCommand extends AbstractCommand {

    private Map<String, AbstractCommand> map = new HashMap<>();
    private AbstractCommand defaultAction;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        String commandPart = getCurrentCommandPart(context).toLowerCase();

        // try to follow map
        if (map.containsKey(commandPart)) {
            context.setIndex(context.getIndex() + 1);
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
