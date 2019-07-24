package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.services.SimpleCrudeService;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public abstract class AttachCommand<P,C> extends UpdateCommand<P> {
    private SimpleCrudeService<C> childService;

    @Override
    public P updateItem(CommandContext context, P parent) throws DmFriendGeneralServiceException {
        C child = getChildItem(context);
        parent = updateItem(context, parent, child);

        return parent;
    }

    public C getChildItem(CommandContext context) throws DmFriendGeneralServiceException {
        String name = getCurrentCommandPart(context, 1);
        C child = childService.read(name);
        return child;
    }

    public abstract P updateItem(CommandContext context, P parent, C child) throws DmFriendGeneralServiceException;



}
