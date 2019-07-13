package net.dalamori.GMFriend.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.interpreter.AbstractCommand;
import net.dalamori.GMFriend.interpreter.AttachCommand;
import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.interpreter.CreateCommand;
import net.dalamori.GMFriend.interpreter.DeleteCommand;
import net.dalamori.GMFriend.interpreter.DisplayCommand;
import net.dalamori.GMFriend.interpreter.InfoCommand;
import net.dalamori.GMFriend.interpreter.MapCommand;
import net.dalamori.GMFriend.interpreter.UpdateCommand;
import net.dalamori.GMFriend.interpreter.printer.PrinterFactory;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.services.LocationService;
import net.dalamori.GMFriend.services.NoteService;
import net.dalamori.GMFriend.services.PropertyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@Configuration
public class InterpreterConfig {

    @Autowired
    private DmFriendConfig config;

    @Autowired
    private NoteService noteService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PropertyService propertyService;

    private AbstractCommand rootCommand;
    private PrinterFactory printerFactory;

    private static final AbstractCommand DO_NOTHING = new AbstractCommand() {
        @Override
        public void handle(CommandContext context) throws InterpreterException {
            return;
        }
    };

    @Bean
    public PrinterFactory printerFactory() {
        if (printerFactory == null) {
            printerFactory = new PrinterFactory();
            printerFactory.setConfig(config);
        }

        return printerFactory;
    }

    /* Root Command Menu (Unprefixed master) */
    private MapCommand unprefixedRoot() {
        MapCommand unprefixedRoot = new MapCommand();
        Map<String, AbstractCommand> commandMap = unprefixedRoot.getMap();
        unprefixedRoot.setDefaultAction(DO_NOTHING);

        String bulletPrefix = config.getInterpreterPrinterBullet() + " " + config.getInterpreterCommandPrefix();

        String rootHelp = "GM's Friend Main Help:\n" + config.getInterpreterPrinterHr() +
                "__Commands__:\n" +
                bulletPrefix + "go [LOCATION_NAME/ID] - shortcut for \"location move\"; sets $HERE\n" +
                bulletPrefix + "help - displays this message\n" +
                bulletPrefix + "here - shortcut for \"location here\"; displays $HERE\n" +
                bulletPrefix + "location [...] - Location commands; see \"location help\" for more info\n" +
                bulletPrefix + "mobile [...] - Mobile commands; see \"mobile help\" for more info\n" +
                bulletPrefix + "note [...] - Note commands; see \"note help\" for more info\n" +
                "\n\r";
        InfoCommand help = new InfoCommand();
        help.setInfo(rootHelp);

        // Root-level Commands
        commandMap.put("go", locationMove());
        commandMap.put("help", help);
        commandMap.put("here", locationHere());
        commandMap.put("location", location());
        commandMap.put("note", note());
        commandMap.put("ping", ping());

        // aliases
        commandMap.put("?", commandMap.get("help"));
        commandMap.put("room", commandMap.get("location"));

        return unprefixedRoot;
    }

    @Bean
    public AbstractCommand rootCommand() {
        // make sure printerFactory has been initialized
        printerFactory();

        if (rootCommand == null) {
            // construct a new
            String commandPrefix = config.getInterpreterCommandPrefix();
            MapCommand unprefixedRoot = unprefixedRoot();
            MapCommand root = new MapCommand();


            // copy all un-prefixed commands, and prefix them;
            for (Map.Entry<String, AbstractCommand> entry : unprefixedRoot.getMap().entrySet()) {
                root.getMap().put(commandPrefix.concat(entry.getKey()), entry.getValue());
            }
            root.getMap().put(commandPrefix, unprefixedRoot);

            // no-op command to absorb all non-actions
            root.setDefaultAction(DO_NOTHING);

            rootCommand = root;
        }

        return rootCommand;
    }

    private AbstractCommand location() {
        MapCommand locationHandler = new MapCommand();
        InfoCommand locationInfo = new InfoCommand();
        String bullet = config.getInterpreterPrinterBullet();

        // make sure that printerFactory has been init'd

        String locationHelp = "location help:\n" +
                config.getInterpreterPrinterHr() +
                "__Subcommands__:\n" +
                bullet + " location help - shows this messagen\n" +
                bullet + " location here - shows $HERE\n" +
                bullet + " location link [ID/NAME] [DESCRIPTION...] - adds a link to the given destination from $HERE\n" +
                bullet + " location move [ID/NAME] - sets $HERE\n" +
                bullet + " location new [NAME] - creates a new location\n" +
                bullet + " location note [LOCATION_NAME/ID] [NOTE_NAME/ID]\n" +
                bullet + " location remove [ID/NAME] - deletes a location\n" +
                bullet + " location show [ID/NAME] - shows a location\n" +
                bullet + " location unlink [ID/NAME] - removes the link from $HERE to the given location\n" +
                bullet + " location unnote [LOCATION_NAME/ID] [NOTE_NAME/ID]\n" +
                "\n\r";

        // HELP
        locationInfo.setInfo(locationHelp);
        locationHandler.setDefaultAction(locationInfo);

        // HERE
        locationHandler.getMap().put("here", locationHere());

        // LINK
        locationHandler.getMap().put("link", locationLink());

        // MOVE
        AbstractCommand move = locationMove();
        locationHandler.getMap().put("move", move);
        locationHandler.getMap().put("go", move);

        // NEW
        CreateCommand<Location> create = new CreateCommand<Location>() {
            @Override
            public Location buildItem(CommandContext context) {
                Location item = new Location();
                item.setOwner(context.getOwner());
                item.setName(getCurrentCommandPart(context));
                item.setPrivacy(PrivacyType.NORMAL);

                return item;
            }
        };
        create.setPrinter(printerFactory.getLocationPrinter());
        create.setService(locationService);
        locationHandler.getMap().put("new", create);
        locationHandler.getMap().put("+", create);

        // NOTE
        locationHandler.getMap().put("note", locationNote());

        // REMOVE
        DeleteCommand<Location> remove = new DeleteCommand<>();
        remove.setService(locationService);
        locationHandler.getMap().put("remove", remove);
        locationHandler.getMap().put("delete", remove);

        // SHOW
        DisplayCommand<Location> show = new DisplayCommand<>();
        show.setPrinter(printerFactory.getLocationPrinter());
        show.setService(locationService);
        locationHandler.getMap().put("show", show);

        // UN-LINK
        locationHandler.getMap().put("unlink", locationRemoveLink());

        // UN-NOTE
        locationHandler.getMap().put("unnote", locationRemoveNote());

        return locationHandler;
    }


    private AbstractCommand locationHere() {
        DisplayCommand<Location> here = new DisplayCommand<Location>(){
            @Override
            public Location getItem(CommandContext context) throws DmFriendGeneralServiceException {
                List<Property> globalProps = propertyService.getGlobalProperties();
                Property found = null;

                for (Property prop : globalProps ) {
                    if (prop.getName().equals(config.getLocationHereGlobalName())) {
                        found = prop;
                        break;
                    }
                }

                if (found != null && found.getType() == PropertyType.LOCATION
                        && StringUtils.isNumeric(found.getValue())) {
                    Long id = Long.valueOf(found.getValue());
                    return service.read(id);
                }

                throw new InterpreterException("$HERE not set.");
            }
        };
        here.setPrinter(printerFactory.getLocationPrinter());
        here.setService(locationService);

        return here;
    }

    private AbstractCommand locationLink() {
        AttachCommand<Location, Location> link = new AttachCommand<Location, Location>() {
            @Override
            public Location updateItem(CommandContext context, Location parent, Location child) {
                LocationLink link = new LocationLink();
                link.setOrigin(parent);
                link.setDestination(child);
                link.setShortDescription(getRemainingCommand(context, 1));
                link.setPrivacy(PrivacyType.NORMAL);

                parent.getLinks().add(link);

                return parent;
            }
        };
        link.setChildService(locationService);
        link.setService(locationService);
        link.setPrinter(printerFactory.getLocationPrinter());

        return link;
    }

    private AbstractCommand locationMove() {
        UpdateCommand<Location> move = new UpdateCommand<Location>() {
            @Override
            public Location updateItem(CommandContext context, Location item) {
                return item;
            }

            @Override
            public Location save(Location item) throws DmFriendGeneralServiceException {
                List<Property> globalProps = propertyService.getGlobalProperties();
                Property found = null;

                for(Property prop : globalProps ) {
                    if (prop.getName().equals(config.getLocationHereGlobalName())) {
                        found = prop;
                        break;
                    }
                }

                if (found == null) {
                    // not found, make new and save to list.
                    found = new Property();
                    found.setType(PropertyType.LOCATION);
                    found.setPrivacy(PrivacyType.INTERNAL);
                    found.setName(config.getLocationHereGlobalName());
                    found.setOwner(config.getSystemGroupOwner());
                    found.setValue(Long.toString(item.getId()));

                    found = propertyService.create(found);
                    propertyService.attachToGlobalContext(found);
                } else {
                    // found. update
                    found.setValue(Long.toString(item.getId()));

                    propertyService.update(found);
                }


                return item;
            }
        };
        move.setPrinter(printerFactory.getLocationPrinter());
        move.setService(locationService);

        return move;
    }

    private AbstractCommand locationNote() {
        AttachCommand<Location, Note> note = new AttachCommand<Location, Note>() {
            @Override
            public Location updateItem(CommandContext context, Location parent, Note child) {
                context.getData().put("child", child);
                parent.getNotes().add(child);
                return parent;
            }

            @Override
            public void afterSave(CommandContext context, Location item) throws DmFriendGeneralServiceException {
                Note note = (Note) context.getData().get("child");

                if (note != null) {
                    try {
                        noteService.detachFromGlobalContext(note);
                    } catch (NoteException ex) {
                        log.debug("Location note command - failed to detach note #{} from global list", note.getId(), ex);
                    }
                }
            }
        };
        note.setService(locationService);
        note.setChildService(noteService);
        note.setPrinter(printerFactory.getLocationPrinter());

        return note;
    }

    private AbstractCommand locationRemoveLink() {
        AttachCommand<Location, Location> unLink = new AttachCommand<Location, Location>() {
            @Override
            public Location updateItem(CommandContext context, Location parent, Location child) {

                Iterator<LocationLink> iterator = parent.getLinks().iterator();
                while (iterator.hasNext()) {
                    LocationLink link = iterator.next();
                    if (link.getDestination().getId() == child.getId()) {
                        iterator.remove();
                        break;
                    }
                }
                return parent;
            }
        };
        unLink.setChildService(locationService);
        unLink.setService(locationService);
        unLink.setPrinter(printerFactory.getLocationPrinter());

        return unLink;
    }

    private AbstractCommand locationRemoveNote() {
        AttachCommand<Location, Note> unNote = new AttachCommand<Location, Note>() {
            @Override
            public Location updateItem(CommandContext context, Location parent, Note child) {
                Iterator<Note> iterator = parent.getNotes().iterator();
                while(iterator.hasNext()) {
                    if (child.equals(iterator.next())) {
                        iterator.remove();
                        break;
                    }
                }
                return parent;
            }

            @Override
            public void afterSave(CommandContext context, Location item) throws DmFriendGeneralServiceException {

                if (context.getData().containsKey("child")) {
                    Note note = (Note) context.getData().get("child");

                    try {
                        noteService.attachToGlobalContext(note);
                    } catch (NoteException ex) {
                        log.debug("location unnote command: failed to re-attach note to global list", ex);
                    }
                }
            }
        };
        unNote.setService(locationService);
        unNote.setChildService(noteService);
        unNote.setPrinter(printerFactory.getLocationPrinter());

        return unNote;
    }


    private AbstractCommand mobile() {
        MapCommand mobileHandler = new MapCommand();
        InfoCommand mobileInfo = new InfoCommand();
        String bullet = config.getInterpreterPrinterBullet();

        String mobileHelp = "mobile help:\n" +
                config.getInterpreterPrinterHr() +
                "__Subcommands__:\n" +
                bullet + " mobile blank [NAME] [HP*] [INITIATIVE*] - creates a new mobile, optionally with hp and init\n" +
                bullet + " mobile damage [ID/NAME] [AMOUNT] - reduces a mobiles HP by an amount\n" +
                bullet + " mobile heal [ID/NAME] [AMOUNT] - increases a mobiles HP by an amount\n" +
                bullet + " mobile new [CREATURE_NAME/ID] [INITIATIVE*] - creates a new mobile from a creature template\n" +
                bullet + " mobile kill [ID/NAME] - insta-kill a mobile\n" +
                bullet + " mobile restore [ID/NAME] - resets a mobile to alive, and full hp\n" +
                "\n\r";

        mobileInfo.setInfo(mobileHelp);
        mobileHandler.setDefaultAction(mobileInfo);

        return mobileHandler;
    }


    private AbstractCommand note() {
        MapCommand noteHandler = new MapCommand();
        InfoCommand noteInfo = new InfoCommand();
        String bullet = config.getInterpreterPrinterBullet();

        String noteHelp = "note help:\n" +
                config.getInterpreterPrinterHr() +
                "__Subcommands__:\n" +
                bullet + " note append [ID/NAME] [CONTENT...] - adds add'l content to the end of a note\n" +
                bullet + " note help - show this message\n" +
                bullet + " note list - lists global notes\n" +
                bullet + " note new [NAME] [CONTENT...] - creates a new note\n" +
                bullet + " note remove [ID/NAME] - deletes a note\n" +
                bullet + " note set [ID/NAME] [CONTENT...] - updates a note\n" +
                bullet + " note show [ID/NAME] - Shows a note\n" +
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
        append.setPrinter(printerFactory.getNotePrinter());
        append.setService(noteService);
        noteHandler.getMap().put("append", append);
        noteHandler.getMap().put("++", append);

        // NOTE LIST
        DisplayCommand<Iterable<Note>> list = new DisplayCommand<Iterable<Note>>() {
            @Override
            public List<Note> getItem(CommandContext context) throws NoteException {
                return noteService.getGlobalNotes();
            }
        };
        list.setPrinter(printerFactory.getNoteListPrinter());
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
        create.setPrinter(printerFactory.getNotePrinter());
        noteHandler.getMap().put("new", create);
        noteHandler.getMap().put("+", create);

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
        set.setPrinter(printerFactory.getNotePrinter());
        set.setService(noteService);
        noteHandler.getMap().put("set", set);

        // NOTE SHOW
        DisplayCommand<Note> show = new DisplayCommand<>();
        show.setPrinter(printerFactory.getNotePrinter());
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
