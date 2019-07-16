package net.dalamori.GMFriend.interpreter.printer;

import lombok.Data;
import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.exceptions.CreatureException;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.services.CreatureService;

import java.util.Map;

@Data
public class PrinterFactory {

    private DmFriendConfig config;
    private CreatureService creatureService;

    private String HR = "---";
    private String BULLET = "x ";

    public void setConfig(DmFriendConfig config) {
        HR = config.getInterpreterPrinterHr();
        BULLET = config.getInterpreterPrinterBullet();
        this.config = config;
    }


    public PrettyPrinter<Creature> getCreaturePrinter() {


        return new PrettyPrinter<Creature>() {
            @Override
            public String print(Creature creature) {
                String output = String.format("[Creature #%d] **%s**\n", creature.getId(), creature.getName()) +
                        HR;

                if (creature.getPropertyMap().size() > 0) {
                    output += "__Properties__:\n";

                    for (Map.Entry<String, Property> entry : creature.getPropertyMap().entrySet()) {
                        output = printProperty(output, entry.getValue());
                    }
                }

                output += HR + String.format("by: %s\n\r", creature.getOwner());

                return output;
            }
        };
    }


    public PrettyPrinter<Location> getLocationPrinter() {
        return new PrettyPrinter<Location>() {
            @Override
            public String print(Location location) {
                String output = String.format("[Location #%d] **%s**\n", location.getId(), formatName(location.getName())) +
                        HR;

                if (location.getNotes().size() > 0) {

                    output += "__Notes__:\n";
                    for(Note note : location.getNotes()) {
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


    public PrettyPrinter<Mobile> getMobilePrinter() {
        return new PrettyPrinter<Mobile>() {
            @Override
            public String print(Mobile mobile) {
                String output = String.format("[Mobile #%d] **%s**\n", mobile.getId(), mobile.getName());
                if (mobile.getCreatureId() != null) {
                    Long creatureId = mobile.getCreatureId();
                    output += String.format("**Creature Type**: ([C#%d] %s)\n", creatureId, getCreatureName(creatureId));
                }
                output += HR + String.format("__Status__:\n%1$s **HP**: (%2$d/%3$d) \n%1$s **Init**: %4$d \n%1$s **Pos**: %5$s\n\r",
                        BULLET, mobile.getHp(), mobile.getMaxHp(), mobile.getInitiative(), mobile.getPosition());

                if (mobile.getPropertyMap().size() > 0) {
                    output += "__Properties__:\n";
                    for (Map.Entry<String, Property> entry : mobile.getPropertyMap().entrySet()) {
                        output = printProperty(output, entry.getValue());
                    }
                }

                output += HR + String.format("by: %s\n\r", mobile.getOwner());

                return output;
            }
        };
    }

    public PrettyPrinter<Note> getNotePrinter() {
        return new PrettyPrinter<Note>() {
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


    public PrettyPrinter<Iterable<Note>> getNoteListPrinter() {
        return new PrettyPrinter<Iterable<Note>>() {
            @Override
            public String print(Iterable<Note> noteList) {
                PrettyPrinter<Note> notePrinter = getNotePrinter();
                int index = 0;
                String output = HR;
                for (Note note : noteList) {
                    index++;
                    output = output
                            .concat(String.format("**[#%d]:**\n", index))
                            .concat(notePrinter.print(note))
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

    private String getCreatureName(Long id) {
        try {
            Creature creature = creatureService.read(id);
            return creature.getName();
        } catch(CreatureException ex) {
            return "Not_Found";
        }
    }

    private String printProperty(String output, Property property) {
        String typeIndicator = "";
        switch (property.getType()) {
            case CREATURE:  typeIndicator = "(Creature) ";  break;
            case LOCATION:  typeIndicator = "(Location) ";  break;
            case MOBILE:    typeIndicator = "(Mobile) ";    break;
            case NOTE:      typeIndicator = "(Note) ";      break;
        }

        return output.concat(String.format("%s **%s**: %s%s\n",
                BULLET, property.getName(), typeIndicator, property.getValue()));
    }
}
