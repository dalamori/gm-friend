package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.interpreter.printer.PrettyPrinter;
import net.dalamori.GMFriend.services.SimpleCrudeService;
import org.apache.commons.lang3.StringUtils;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public abstract class UpdateCommand<T> extends AbstractCommand {

    protected SimpleCrudeService<T> service;
    protected PrettyPrinter<T> printer;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        T item = null;
        try {
            item = getItem(context);
            item = updateItem(context, item);
            item = service.update(item);
            afterSave(item);
            context.setResponse(printer.print(item));
        } catch (DmFriendGeneralServiceException ex) {
            log.debug("UpdateCommand::handle failed to update {} item", item, ex);
            throw new InterpreterException("Failed to update", ex);
        }
    }

    public T getItem(CommandContext context) throws DmFriendGeneralServiceException {
        String name = getCurrentCommandPart(context);

        if (StringUtils.isNumeric(name)) {
            Long id = Long.valueOf(name);

            return service.read(id);
        }

        return service.read(name);
    }

    public void afterSave(T item) throws DmFriendGeneralServiceException {
        return;
    }

    public abstract T updateItem(CommandContext context, T item);

}
