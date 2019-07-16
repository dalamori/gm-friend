package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.models.interfaces.HasProperties;

@Data
@EqualsAndHashCode(callSuper = true)
public class PropertyDeleteCommand<T extends HasProperties> extends UpdateCommand<T> {

    @Override
    public T updateItem(CommandContext context, T item) throws DmFriendGeneralServiceException {
        String propertyName = getCurrentCommandPart(context, 1);
        if (!item.getPropertyMap().containsKey(propertyName)) {
            throw new InterpreterException("can't delete property which doesn't exist");
        }
        item.getPropertyMap().remove(propertyName);

        return item;
    }

}
