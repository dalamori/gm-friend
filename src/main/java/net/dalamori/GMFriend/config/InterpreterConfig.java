package net.dalamori.GMFriend.config;

import lombok.Data;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.InfoCommand;
import net.dalamori.GMFriend.interpreter.MapCommand;
import net.dalamori.GMFriend.services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class InterpreterConfig {

    @Autowired
    private DmFriendConfig config;

    @Autowired
    private NoteService noteService;

    private AbstractCommand rootCommand;

    @Bean
    public AbstractCommand rootCommand() {
        if (rootCommand == null) {
            // construct a new
            String commandPrefix = config.getInterpreterCommandPrefix();

            MapCommand root = new MapCommand();
            root.getMap().put(commandPrefix.concat("ping"), ping());
            root.getMap().put(commandPrefix.concat("note"), note());

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

        noteInfo.setInfo(noteHelp);

        noteHandler.setDefaultAction(noteInfo);

        return noteHandler;
    }

    private AbstractCommand ping() {
        InfoCommand pingHandler = new InfoCommand();
        pingHandler.setInfo("Pong!");

        return pingHandler;
    }
}
