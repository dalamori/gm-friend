package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.services.SimpleCrudeService;

@Data
@EqualsAndHashCode(callSuper = false)
public class DisplayCommand<T> extends AbstractCommand {
    protected PrettyPrinter<T> printer;
    protected SimpleCrudeService<T> service;

    private static final String NOT_FOUND = "Sorry, that item was not found.";

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        try {
            printer.print(getItem(context));
        } catch (DmFriendGeneralServiceException ex) {
            throw new InterpreterException("failed to display", ex);
        }
    }

    public T getItem(CommandContext context) throws DmFriendGeneralServiceException {
        String name = getCurrentCommandPart(context);
        return service.read(name);
    }
}
