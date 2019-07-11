package net.dalamori.GMFriend.interpreter.printer;

import lombok.Data;
import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;

@Data
public class PrinterFactory {

    private DmFriendConfig config;

    private String HR = "---";
    private String BULLET = "x ";

    public void setConfig(DmFriendConfig config) {
        HR = config.getInterpreterPrinterHr();
        BULLET = config.getInterpreterPrinterBullet();
        this.config = config;
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


    public PrettyPrinter<Location> getLocationPrinter() {
        return new PrettyPrinter<Location>() {
            @Override
            public String print(Location object) {
                String output = String.format("[Location #%d] **%s**\n", formatName(object.getName())) +
                        HR;

                if (object.getNotes().size() > 0) {

                    output += "__Notes__:\n";
                    for(Note note : object.getNotes()) {
                        output += String.format("%s [N#%d] **%s**: %s\n", BULLET, note.getId(), formatName(note.getTitle()), truncate(note.getBody(), 64));
                    }
                }

                if (object.getLinks().size() > 0) {

                }


                return output;
            }
        };
    }

}
