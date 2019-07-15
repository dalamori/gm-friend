package net.dalamori.GMFriend.interpreter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.models.interfaces.HasProperties;
import net.dalamori.GMFriend.services.CreatureService;
import net.dalamori.GMFriend.services.LocationService;
import net.dalamori.GMFriend.services.MobileService;
import net.dalamori.GMFriend.services.NoteService;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@EqualsAndHashCode(callSuper = true)
public class PropertySetCommand<T extends HasProperties> extends UpdateCommand<T> {

    private NoteService noteService;
    private LocationService locationService;
    private MobileService mobileService;
    private CreatureService creatureService;

    @Override
    public T updateItem(CommandContext context, T item) throws DmFriendGeneralServiceException {
        String propertyName = getCurrentCommandPart(context, 1);
        Property property;

        if (propertyName.length() <=  0) {
            throw new InterpreterException("Can't parse property name");
        } else {
            if (item.getPropertyMap().containsKey(propertyName)) {
                // fetch existing
                property = item.getPropertyMap().get(propertyName);
            } else {
                // make new
                property = new Property();
                property.setName(propertyName);
                property.setOwner(context.getOwner());
                property.setPrivacy(PrivacyType.NORMAL);
            }

            // first, check numeric types
            String value = getRemainingCommand(context, 1);
            if (value.matches("^[\\d]+(?:[.]\\d+)?$")) {
                // numbers, check int vs decimal
                if (StringUtils.isNumeric(value)) {
                    property.setType(PropertyType.INTEGER);
                } else {
                    property.setType(PropertyType.DECIMAL);
                }
            } else switch (getCurrentCommandPart(context, 2).toLowerCase()) {
                // next, see if the next word is a keyword
                case "note":
                    String noteId = getCurrentCommandPart(context, 3);
                    if (noteService.exists(noteId)) {
                        Note targetNote = noteService.read(noteId);
                        property.setType(PropertyType.NOTE);
                        value = targetNote.getTitle();
                    }
                    break;

                case "location":
                    String locationId = getCurrentCommandPart(context, 3);
                    if (locationService.exists(locationId)) {
                        Location targetLocation = locationService.read(locationId);
                        property.setType(PropertyType.LOCATION);
                        value = targetLocation.getName();
                    }
                    break;

                case "mobile":
                    String mobileId = getCurrentCommandPart(context, 3);
                    if (mobileService.exists(mobileId)) {
                        Mobile targetMobile = mobileService.read(mobileId);
                        property.setType(PropertyType.MOBILE);
                        value = targetMobile.getName();
                    }
                    break;

                case "creature":
                    String creatureId = getCurrentCommandPart(context, 3);
                    if (creatureService.exists(creatureId)) {
                        Creature targetCreature = creatureService.read(creatureId);
                        property.setType(PropertyType.CREATURE);
                        value = targetCreature.getName();
                    }
                    break;

                case "add":
                case "++":
                    if (property.getId() == null) {
                        throw new InterpreterException("Can't increment unsaved property");
                    }
                    switch (property.getType()) {
                        case INTEGER:
                            String integerArg = getCurrentCommandPart(context, 3);
                            BigInteger curIntegerValue = new BigInteger(property.getValue());
                            BigInteger addIntegerValue;
                            if (StringUtils.isNumeric(integerArg)) {
                                addIntegerValue = new BigInteger(integerArg);
                            } else {
                                addIntegerValue = BigInteger.ONE;
                            }
                            value = curIntegerValue.add(addIntegerValue).toString();

                            break;

                        case DECIMAL:
                            String decimalArg = getCurrentCommandPart(context, 3);
                            BigDecimal curDecimalValue = new BigDecimal(property.getValue());
                            BigDecimal addDecimalValue;
                            if (StringUtils.isNumeric(decimalArg)) {
                                addDecimalValue = new BigDecimal(decimalArg);
                            } else {
                                addDecimalValue = BigDecimal.ONE;
                            }
                            value = curDecimalValue.add(addDecimalValue).toString();

                            break;

                        default:
                            throw new InterpreterException("Can't increment non-numeric property");

                    }
                    break;

                case "subtract":
                case "--":
                    if (property.getId() == null) {
                        throw new InterpreterException("Can't decrement unsaved property");
                    }
                    switch (property.getType()) {
                        case INTEGER:
                            String integerArg = getCurrentCommandPart(context, 3);
                            BigInteger curIntegerValue = new BigInteger(property.getValue());
                            BigInteger addIntegerValue;
                            if (StringUtils.isNumeric(integerArg)) {
                                addIntegerValue = new BigInteger(integerArg);
                            } else {
                                addIntegerValue = BigInteger.ONE;
                            }
                            value = curIntegerValue.subtract(addIntegerValue).toString();

                            break;

                        case DECIMAL:
                            String decimalArg = getCurrentCommandPart(context, 3);
                            BigDecimal curDecimalValue = new BigDecimal(property.getValue());
                            BigDecimal addDecimalValue;
                            if (StringUtils.isNumeric(decimalArg)) {
                                addDecimalValue = new BigDecimal(decimalArg);
                            } else {
                                addDecimalValue = BigDecimal.ONE;
                            }
                            value = curDecimalValue.subtract(addDecimalValue).toString();

                            break;

                        default:
                            throw new InterpreterException("Can't decrement non-numeric property");

                    }
                    break;

                default:
                    if (StringUtils.isBlank(value)) {
                        throw new InterpreterException("can't set empty value; delete it instead");
                    }
                    // lastly, just save it as a string...
                    property.setType(PropertyType.STRING);
            }

            property.setValue(value);
            if (property.getType() == null) {
                property.setType(PropertyType.STRING);
            }

            item.getPropertyMap().put(propertyName, property);
        }

        return item;
    }
}
