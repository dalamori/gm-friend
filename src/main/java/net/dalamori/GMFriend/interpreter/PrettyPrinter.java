package net.dalamori.GMFriend.interpreter;

import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;
public abstract class PrettyPrinter<T> {

    public static final String HR = "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n";

    public abstract String print(T object);

    public static PrettyPrinter<Note> getNotePrinter() {
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

    public static PrettyPrinter<Iterable<Note>> getNoteListPrinter() {
        return new PrettyPrinter<Iterable<Note>>() {
            @Override
            public String print(Iterable<Note> noteList) {
                PrettyPrinter<Note> notePrinter = PrettyPrinter.getNotePrinter();
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


    public static PrettyPrinter<Location> getLocationPrinter() {
        return new PrettyPrinter<Location>() {
            @Override
            public String print(Location object) {
                String output = String.format("[Location #%d] **%s**\n", formatName(object.getName())) + HR;

                if (object.getNotes().size() > 0) {

                    output += "__Notes__:\n";
                    for(Note note : object.getNotes()) {
                        output += String.format("- [N#%d] **%s**: %s\n", note.getId(), formatName(note.getTitle()), truncate(note.getBody(), 64));
                    }
                }




                return output;
            }
        };
    }

    public static String formatName(String input) {
        return input.replaceAll("_", " ");

    }

    public static String truncate(String input, int length) {
        if (input.length() <= length ) {
            return input;
        }

        return input.substring(0, length).concat("...");
    }

}
