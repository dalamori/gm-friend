package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.services.SimpleCrudeService;
import org.apache.commons.lang3.StringUtils;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public abstract class AttachCommand<P,C> extends UpdateCommand<P> {
    private SimpleCrudeService<C> childService;

    @Override
    public P updateItem(CommandContext context, P parent) {
        C child;
        try {
            child = getChildItem(context);
            parent = updateItem(context, parent, child);
        } catch (DmFriendGeneralServiceException ex) {
            log.debug("AttachCommand::updateItem - unable to lookup child", ex);
            context.setResponse("Child item not found");
        }

        return parent;
    }

    public C getChildItem(CommandContext context) throws DmFriendGeneralServiceException {
        String name = getCurrentCommandPart(context, 1);
        C child;

        if (StringUtils.isNumeric(name)) {
            child = childService.read(Long.valueOf(name));
        } else {
            child = childService.read(name);
        }

        return child;
    }

    public abstract P updateItem(CommandContext context, P parent, C child);



}
