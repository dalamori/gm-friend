package net.dalamori.GMFriend.interpreter.printer;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.Data;
import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.CreatureException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.services.CreatureService;
import net.dalamori.GMFriend.services.PropertyService;

import java.util.Map;

@Data
public class PrinterFactory {

    private DmFriendConfig config;
    private CreatureService creatureService;
    private PropertyService propertyService;

    private PrettyPrinter<Creature> creaturePrinter;
    private PrettyPrinter<Iterable<Mobile>> initiativeListPrinter;
    private PrettyPrinter<Location> locationPrinter;
    private PrettyPrinter<Mobile> mobilePrinter;
    private PrettyPrinter<Note> notePrinter;
    private PrettyPrinter<Iterable<Note>> noteListPrinter;
    private PrettyPrinter<Property> propertyPrinter;
    private PrettyPrinter<Map<String, Property>> propertyMapPrinter;


    private String HR = "---";
    private String BULLET = "x ";

    public void setConfig(DmFriendConfig config) {
        HR = config.getInterpreterPrinterHr();
        BULLET = config.getInterpreterPrinterBullet();
        this.config = config;
    }


    public PrettyPrinter<Creature> getCreaturePrinter() {
        PrettyPrinter<Map<String, Property>> propPrinter = getPropertyMapPrinter();

        if (creaturePrinter == null) {
            creaturePrinter = new PrettyPrinter<Creature>() {
                @Override
                public String print(Creature creature) {
                    String output = String.format("[Creature #%d] **%s**\n", creature.getId(), creature.getName()) +
                            HR;

                    if (creature.getPropertyMap().size() > 0) {
                        output += propPrinter.print(creature.getPropertyMap());
                    }

                    output += HR + String.format("by: %s\n\r", creature.getOwner());

                    return output;
                }
            };
        }
        return creaturePrinter;
    }

    public PrettyPrinter<Iterable<Mobile>> getInitiativeListPrinter() {
        if (initiativeListPrinter == null) {
            initiativeListPrinter = new PrettyPrinter<Iterable<Mobile>>() {
                @Override
                public String print(Iterable<Mobile> mobileList) {

                    // $ACTIVE lookup
                    int activeInit = 0;
                    String activeName = "";
                    try {
                        Property active = propertyService.getGlobalProperties().getOrDefault(config.getMobileActiveGlobalName(), null);
                        if (active != null) {
                            int indexOfDelimiter = active.getValue().indexOf('|');
                            if (indexOfDelimiter > 0) {
                                activeInit = Integer.valueOf(active.getValue().substring(0,indexOfDelimiter));
                                activeName = active.getValue().substring(indexOfDelimiter + 1);
                            }

                        }
                    } catch (PropertyException ex) {
                        activeInit = 0;
                        activeName = "";
                    }

                    // Output
                    StringBuilder output = new StringBuilder();
                    output.append(HR);
                    boolean activeFound = false;
                    for (Mobile mobile : mobileList) {
                        // check for active interstitial, and print empty line if needed.
                        if (!activeFound) {
                            if (activeInit <= mobile.getInitiative()) {
                                if (mobile.getName().compareToIgnoreCase(activeName) < 0) {
                                    // init pointer is before mobile; draw focus line
                                    output.append(String.format("%s (%d) -- No Active Mobile --\n",
                                            config.getInterpreterPrinterEmphasisBullet(), activeInit));
                                    activeFound = true;

                                    // show list entry with next notation
                                    output.append(String.format("%s (%d) %s (Next Active)\n",
                                            config.getInterpreterPrinterBullet(),
                                            mobile.getInitiative(),
                                            mobile.getName()));
                                } else if (mobile.getName().compareToIgnoreCase(activeName) == 0) {
                                    // init pointer hit
                                    output.append(String.format("%s (%d) %s (Active)\n",
                                            config.getInterpreterPrinterEmphasisBullet(),
                                            mobile.getInitiative(),
                                            mobile.getName()));
                                    activeFound = true;
                                } else {
                                    // mob is tied w/ init pointer, but has already taken turn.
                                    output.append(String.format("%s (%d) %s\n",
                                            config.getInterpreterPrinterBullet(),
                                            mobile.getInitiative(),
                                            mobile.getName()));
                                }
                            } else {
                                // mob has init lower than active, has already taken turn.
                                output.append(String.format("%s (%d) %s\n",
                                        config.getInterpreterPrinterBullet(),
                                        mobile.getInitiative(),
                                        mobile.getName()));
                            }
                        } else {
                            // active was already found
                            output.append(String.format("%s (%d) %s\n",
                                    config.getInterpreterPrinterBullet(),
                                    mobile.getInitiative(),
                                    mobile.getName()));
                        }

                    }

                    return output.toString();
                }
            };
        }
        return initiativeListPrinter;
    }


    public PrettyPrinter<Location> getLocationPrinter() {
        if (locationPrinter == null) {
            locationPrinter = new PrettyPrinter<Location>() {
                @Override
                public String print(Location location) {
                    String output = String.format("[Location #%d] **%s**\n", location.getId(), formatName(location.getName())) +
                            HR;

                    if (location.getNotes().size() > 0) {

                        output += "__Notes__:\n";
                        for (Note note : location.getNotes()) {
                            output += String.format("%s [N#%d] **%s**: %s\n",
                                    BULLET,
                                    note.getId(),
                                    formatName(note.getTitle()),
                                    truncate(note.getBody(), 64));
                        }
                    }

                    if (location.getLinks().size() > 0) {
                        output += "__Links__:\n";
                        for (LocationLink link : location.getLinks()) {
                            output += String.format("%s [L#%d] **%s**: %s\n",
                                    BULLET,
                                    link.getDestination().getId(),
                                    link.getDestination().getName(),
                                    link.getShortDescription());
                        }
                    }

                    output += HR + String.format("by: %s\n\r", location.getOwner());

                    return output;
                }
            };
        }
        return locationPrinter;
    }


    public PrettyPrinter<Mobile> getMobilePrinter() {
        PrettyPrinter<Map<String,Property>> propPrinter = getPropertyMapPrinter();
        if (mobilePrinter == null) {
            mobilePrinter = new PrettyPrinter<Mobile>() {
                @Override
                public String print(Mobile mobile) {
                    String output = String.format("[Mobile #%d] **%s**\n", mobile.getId(), mobile.getName());
                    if (mobile.getCreatureId() != null) {
                        Long creatureId = mobile.getCreatureId();
                        output += String.format("**Creature Type**: [Creature #%d] %s\n", creatureId, getCreatureName(creatureId));
                    }
                    output += HR + String.format("__Status__:\n" +
                                    "%1$s **HP**: (**%2$d**/%3$d) \n" +
                                    "%1$s **Initiative**: %4$d \n" +
                                    "%1$s **Position**: %5$s\n",
                            BULLET, mobile.getHp(), mobile.getMaxHp(), mobile.getInitiative(), mobile.getPosition());

                    if (mobile.getPropertyMap().size() > 0) {
                        output += propPrinter.print(mobile.getPropertyMap());
                    }

                    output += HR + String.format("by: %s\n\r", mobile.getOwner());

                    return output;
                }
            };
        }
        return mobilePrinter;
    }

    public PrettyPrinter<Note> getNotePrinter() {
        if (notePrinter == null) {
            notePrinter = new PrettyPrinter<Note>() {
                @Override
                public String print(Note note) {
                    String output = String.format("[Note #%d] **%s**\n", note.getId(), formatName(note.getTitle())) +
                            HR +
                            note.getBody() + "\n" +
                            HR +
                            String.format("by: *%s*\n\r", note.getOwner());

                    return output;
                }
            };
        }
        return notePrinter;
    }


    public PrettyPrinter<Iterable<Note>> getNoteListPrinter() {
        if (noteListPrinter == null) {
            noteListPrinter = new PrettyPrinter<Iterable<Note>>() {
                @Override
                public String print(Iterable<Note> noteList) {
                    int index = 0;
                    String output = HR;
                    for (Note note : noteList) {
                        index++;
                        output = output
                                .concat(String.format("%s **[N#%d]** %s\n", BULLET, note.getId(), note.getTitle()))
                                .concat("\n\r");
                    }

                    // empty fallback
                    if (!noteList.iterator().hasNext()) {
                        output = output + "Empty List";
                    }

                    return output;
                }
            };
        }
        return noteListPrinter;
    }

    public PrettyPrinter<Property> getPropertyPrinter() {
        if (propertyPrinter == null) {
            propertyPrinter = new PrettyPrinter<Property>() {
                @Override
                public String print(Property property) {
                    String typeIndicator = "";
                    switch (property.getType()) {
                        case CREATURE:  typeIndicator = "(Creature) ";  break;
                        case LOCATION:  typeIndicator = "(Location) ";  break;
                        case MOBILE:    typeIndicator = "(Mobile) ";    break;
                        case NOTE:      typeIndicator = "(Note) ";      break;
                    }

                    return String.format("%s **%s**: %s%s\n",
                            BULLET, PrettyPrinter.formatName(property.getName()), typeIndicator, property.getValue());
                }
            };
        }
        return propertyPrinter;
    }

    public PrettyPrinter<Map<String, Property>> getPropertyMapPrinter() {
        if (propertyMapPrinter == null) {

            PrettyPrinter<Property> propPrinter = getPropertyPrinter();
            propertyMapPrinter = new PrettyPrinter<Map<String, Property>>() {
                @Override
                public String print(Map<String, Property> propertyMap) {
                    StringBuilder output = new StringBuilder();
                    output.append("__Properties__:\n");
                    for (Map.Entry<String, Property> entry : propertyMap.entrySet()) {
                        output.append(propPrinter.print(entry.getValue()));
                    }

                    return output.toString();
                }
            };
        }
        return propertyMapPrinter;
    }


    private String getCreatureName(Long id) {
        try {
            Creature creature = creatureService.read(id);
            return creature.getName();
        } catch(CreatureException ex) {
            return "Not_Found";
        }
    }

}
