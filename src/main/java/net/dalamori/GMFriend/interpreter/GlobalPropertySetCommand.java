package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.interpreter.printer.PrettyPrinter;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.models.interfaces.HasProperties;
import net.dalamori.GMFriend.services.PropertyService;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class GlobalPropertySetCommand extends PropertySetCommand<HasProperties> {

    private PropertyService propertyService;
    private PrettyPrinter<Property> propertyPrinter;

    @Override
    public void handle(CommandContext context) throws InterpreterException {
        try {
            Property target = getProperty(context);

            // back the index up one, updateProperty assumes an extra arg we dont have.
            // int index = context.getIndex();
            // context.setIndex(index - 1);
            target = updateProperty(context, target);
            // context.setIndex(index);

            target = saveProperty(target);
            context.setResponse(propertyPrinter.print(target));

        } catch (DmFriendGeneralServiceException ex) {
            throw new InterpreterException("Unable to set global property", ex);
        }
    }

    public Property getProperty(CommandContext context) throws DmFriendGeneralServiceException {
        String name = getCurrentCommandPart(context);
        Map<String, Property> globals = propertyService.getGlobalProperties();
        Property result = globals.getOrDefault(name, null);
        if (result == null) {
            result = new Property();
            result.setOwner(context.getOwner());
            result.setType(PropertyType.UNKNOWN);
            result.setPrivacy(PrivacyType.NORMAL);
            result.setName(name);
        }

        return result;
    }

    public Property saveProperty(Property property) throws PropertyException {
        if (property.getId() == null) {
            property = propertyService.create(property);
            propertyService.attachToGlobalContext(property);
            return property;
        }
        return propertyService.update(property);
    }
}
