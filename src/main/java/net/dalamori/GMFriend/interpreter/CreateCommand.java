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
public abstract class CreateCommand<T> extends AbstractCommand {
    private SimpleCrudeService<T> service;
    private PrettyPrinter<T> printer;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        try {

            T savedItem = service.create(buildItem(context));
            afterSave(savedItem);
            context.setResponse(printer.print(savedItem));
        } catch (DmFriendGeneralServiceException ex) {
            log.debug("CreateCommand::handle Failed to create item", ex);
            throw new InterpreterException("failed to create item", ex);
        }

    }

    public abstract T buildItem(CommandContext context);

    public void afterSave(T obj) throws DmFriendGeneralServiceException {
        return;
    }
}
