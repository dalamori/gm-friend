package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.services.SimpleCrudeService;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class DeleteCommand<T> extends AbstractCommand {

    protected SimpleCrudeService<T> service;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        try {
            T item = getItem(context);
            beforeDelete(item);
            service.delete(item);

            context.setResponse("OK");
        } catch (DmFriendGeneralServiceException ex) {
            log.debug("DeleteCommand::handle unable to delete", ex);
            throw new InterpreterException("unable to delete", ex);
        }
    }

    public T getItem(CommandContext context) throws DmFriendGeneralServiceException {
        String name = getCurrentCommandPart(context);
        return service.read(name);
    }

    public void beforeDelete(T item) throws DmFriendGeneralServiceException {
        return;
    }
}
