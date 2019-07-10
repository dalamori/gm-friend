package net.dalamori.GMFriend.config;

import lombok.Data;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.interpreter.CreateCommand;
import net.dalamori.GMFriend.interpreter.DeleteCommand;
import net.dalamori.GMFriend.interpreter.DisplayCommand;
import net.dalamori.GMFriend.interpreter.InfoCommand;
import net.dalamori.GMFriend.interpreter.MapCommand;
import net.dalamori.GMFriend.interpreter.PrettyPrinter;
import net.dalamori.GMFriend.interpreter.UpdateCommand;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
public class InterpreterConfig {

    @Autowired
    private DmFriendConfig config;

    @Autowired
    private NoteService noteService;

    private AbstractCommand rootCommand;

    /* Root Command Menu */
    private MapCommand unprefixedRoot() {

        MapCommand unprefixedRoot = new MapCommand();
        unprefixedRoot.getMap().put("ping", ping());
        unprefixedRoot.getMap().put("note", note());

        return unprefixedRoot;
    }

    @Bean
    public AbstractCommand rootCommand() {
        if (rootCommand == null) {
            // construct a new
            String commandPrefix = config.getInterpreterCommandPrefix();
            MapCommand unprefixedRoot = unprefixedRoot();
            MapCommand root = new MapCommand();

            for (Map.Entry<String, AbstractCommand> entry : unprefixedRoot.getMap().entrySet()) {
                root.getMap().put(commandPrefix.concat(entry.getKey()), entry.getValue());
            }
            root.getMap().put(commandPrefix, unprefixedRoot);

            rootCommand = root;
        }

        return rootCommand;
    }


    private AbstractCommand note() {
        MapCommand noteHandler = new MapCommand();
        InfoCommand noteInfo = new InfoCommand();
        String prefix = config.getInterpreterCommandPrefix();

        String noteHelp = prefix + "note help:\n\r" +
                "Subcommands:\n" +
                "* note list - lists the notes you own\n" +
                "* note new [NAME] [CONTENT...] - creates a new note\n" +
                "* note remove [NAME] - deletes a note\n" +
                "* note set [NAME] [CONTENT...] - updates a note\n" +
                "* note append [NAME] [CONTENT...] - adds add'l content to the end of a note\n" +
                "* note show [NAME] - Shows a note\n" +
                "* note help - show this message\n" +
                "\n\r";

        // NOTE HELP
        noteInfo.setInfo(noteHelp);
        noteHandler.setDefaultAction(noteInfo);

        // NOTE APPEND
        UpdateCommand<Note> append = new UpdateCommand<Note>() {
            @Override
            public Note updateItem(CommandContext context, Note item) {
                String append = getRemainingCommand(context).concat("\n");
                item.setBody(item.getBody().concat(append));
                return item;
            }
        };
        append.setPrinter(PrettyPrinter.getNotePrinter());
        append.setService(noteService);
        noteHandler.getMap().put("append", append);
        noteHandler.getMap().put("+", append);

        // NOTE LIST
        DisplayCommand<Iterable<Note>> list = new DisplayCommand<Iterable<Note>>() {
            @Override
            public List<Note> getItem(CommandContext context) throws NoteException {
                return noteService.getGlobalNotes();
            }
        };
        list.setPrinter(PrettyPrinter.getNoteListPrinter());
        noteHandler.getMap().put("list", list);

        // NOTE NEW
        CreateCommand<Note> create = new CreateCommand<Note>() {
            @Override
            public Note buildItem(CommandContext context) {
                Note note = new Note();
                note.setOwner(context.getOwner());
                note.setPrivacy(PrivacyType.NORMAL);
                note.setTitle(getCurrentCommandPart(context));
                note.setBody(getRemainingCommand(context).concat("\n"));

                return note;
            }

            @Override
            public void afterSave(Note note) throws NoteException {
                noteService.attachToGlobalContext(note);
            }
        };
        create.setService(noteService);
        create.setPrinter(PrettyPrinter.getNotePrinter());
        noteHandler.getMap().put("new", create);
        noteHandler.getMap().put("create", create);

        // NOTE REMOVE
        DeleteCommand<Note> remove = new DeleteCommand<Note>();
        remove.setService(noteService);
        noteHandler.getMap().put("delete", remove);
        noteHandler.getMap().put("remove", remove);

        // NOTE SET
        UpdateCommand<Note> set = new UpdateCommand<Note>() {
            @Override
            public Note updateItem(CommandContext context, Note item) {
                String value = getCurrentCommandPart(context).concat("\n");
                item.setBody(value);

                return item;
            }
        };
        set.setPrinter(PrettyPrinter.getNotePrinter());
        set.setService(noteService);
        noteHandler.getMap().put("set", set);

        // NOTE SHOW
        DisplayCommand<Note> show = new DisplayCommand<>();
        show.setPrinter(PrettyPrinter.getNotePrinter());
        show.setService(noteService);
        noteHandler.getMap().put("show", show);

        // return
        return noteHandler;
    }

    private AbstractCommand ping() {
        InfoCommand pingHandler = new InfoCommand();
        pingHandler.setInfo("Pong!");

        return pingHandler;
    }
}
