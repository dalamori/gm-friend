package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.interpreter.printer.PrettyPrinter;
import net.dalamori.GMFriend.services.SimpleCrudeService;

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

            item = save(item);
            afterSave(context, item);
            context.setResponse(printer.print(item));
        } catch (DmFriendGeneralServiceException ex) {
            log.debug("UpdateCommand::handle failed to update {} item", item, ex);
            throw new InterpreterException("Failed to update", ex);
        }
    }

    public T getItem(CommandContext context) throws DmFriendGeneralServiceException {
        String name = getCurrentCommandPart(context);
        return service.read(name);
    }

    public T save(T item) throws DmFriendGeneralServiceException {
        return service.update(item);
    }

    public void afterSave(CommandContext context, T item) throws DmFriendGeneralServiceException {
        return;
    }

    public abstract T updateItem(CommandContext context, T item) throws DmFriendGeneralServiceException;

}
