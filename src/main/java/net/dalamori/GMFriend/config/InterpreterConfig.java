package net.dalamori.GMFriend.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dalamori.GMFriend.exceptions.DmFriendGeneralServiceException;
import net.dalamori.GMFriend.exceptions.InterpreterException;
import net.dalamori.GMFriend.exceptions.NoteException;
import net.dalamori.GMFriend.exceptions.PropertyException;
import net.dalamori.GMFriend.interpreter.*;
import net.dalamori.GMFriend.interpreter.printer.PrettyPrinter;
import net.dalamori.GMFriend.interpreter.printer.PrinterFactory;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.models.enums.UserRole;
import net.dalamori.GMFriend.services.*;
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
    private MobileService mobileService;

    @Autowired
    private CreatureService creatureService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private UserService userService;

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
            printerFactory.setCreatureService(creatureService);
            printerFactory.setPropertyService(propertyService);
        }

        return printerFactory;
    }

    /* Root Command Menu (Unprefixed master) */
    private MapCommand unprefixedRoot() {
        MapCommand unprefixedRoot = new MapCommand();
        Map<String, MapSubcommand> commandMap = unprefixedRoot.getMap();
        unprefixedRoot.setDefaultAction(DO_NOTHING);

        String bulletPrefix = config.getInterpreterPrinterBullet() + " " + config.getInterpreterCommandPrefix();

        String rootHelp = "GM's Friend Main Help:\n" + config.getInterpreterPrinterHr() +
                "__Commands__:\n" +
                bulletPrefix + "cret [...] - Creature commands; see \"creature help\" for more info\n" +
                bulletPrefix + "goto [LOCATION_NAME/ID] - shortcut for \"location move\"; sets $HERE\n" +
                bulletPrefix + "help - displays this message\n" +
                bulletPrefix + "here - shortcut for \"location here\"; displays $HERE\n" +
                bulletPrefix + "mob [...] - Mobile commands; see \"mobile help\" for more info\n" +
                bulletPrefix + "note [...] - Note commands; see \"note help\" for more info\n" +
                bulletPrefix + "room [...] - Location commands; see \"location help\" for more info\n" +
                bulletPrefix + "turn [...] - turn commands; manipulates $ACTIVE; see \"turn help\" for more info\n" +
                bulletPrefix + "user [...] - user commands; manipulates users\n" +
                bulletPrefix + "var [...] - global variable commands; see \"var help\" for more info\n" +
                "\n\r";
        InfoCommand help = new InfoCommand();
        help.setInfo(rootHelp);

        // Root-level Commands
        commandMap.put("creature", new MapSubcommand(UserRole.ROLE_STRANGER, "", creature()));
        commandMap.put("goto", new MapSubcommand(UserRole.ROLE_STRANGER, "", locationMove()));
        commandMap.put("help", new MapSubcommand(UserRole.ROLE_STRANGER, "", help));
        commandMap.put("here", new MapSubcommand(UserRole.ROLE_STRANGER, "", locationHere()));
        commandMap.put("location", new MapSubcommand(UserRole.ROLE_STRANGER, "", location()));
        commandMap.put("mobile", new MapSubcommand(UserRole.ROLE_STRANGER, "", mobile()));
        commandMap.put("note", new MapSubcommand(UserRole.ROLE_STRANGER, "", note()));
        commandMap.put("ping", new MapSubcommand(UserRole.ROLE_STRANGER, "", ping()));
        commandMap.put("turn", new MapSubcommand(UserRole.ROLE_STRANGER, "", turn()));
        commandMap.put("user", new MapSubcommand(UserRole.ROLE_ASSISTANT, "", user()));
        commandMap.put("var", new MapSubcommand(UserRole.ROLE_STRANGER, "", var()));

        // aliases
        commandMap.put("?", commandMap.get("help"));
        commandMap.put("cret", commandMap.get("creature"));
        commandMap.put("room", commandMap.get("location"));
        commandMap.put("mob", commandMap.get("mobile"));

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
            for (Map.Entry<String, MapSubcommand> entry : unprefixedRoot.getMap().entrySet()) {
                root.getMap().put(commandPrefix.concat(entry.getKey()), entry.getValue());
            }
            root.getMap().put(commandPrefix, new MapSubcommand(UserRole.ROLE_STRANGER, "", unprefixedRoot));

            // no-op command to absorb all non-actions
            root.setDefaultAction(DO_NOTHING);

            rootCommand = root;
        }

        return rootCommand;
    }

    private AbstractCommand creature() {
        MapCommand creatureHandler = new MapCommand();
        InfoCommand creatureInfo = new InfoCommand();
        String bullet = config.getInterpreterPrinterBullet();

        String creatureHelp = "creature help:\n" +
                config.getInterpreterPrinterHr() +
                "__Subcommands__:\n" +
                bullet + " creature help - shows this message\n" +
                bullet + " creature delete [NAME] - deletes a creature\n" +
                bullet + " creature new [NAME] - creates a new creature\n" +
                bullet + " creature set [CREATURE_NAME/ID] [KEY] [...] - sets a property of a creature; full syntax below\n" +
                bullet + " creature show [CREATURE_NAME/ID] - displays a creature\n" +
                bullet + " creature unset [CREATURE_NAME/ID] [KEY] - deletes a property from the creature\n" +
                "\n" +
                "__Creature Set syntax__:\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] [VALUE] - sets a property of the creature to a given value\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] add [AMOUNT†] - increases a numeric property value by 1, or optionally another amount\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] creature [CREATURE_ID/NAME] - sets a property of the creature to a given creature\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] location [LOCATION_ID/NAME] - sets a property of the creature to a given location\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] creature [MOBILE_ID/NAME] - sets a property of the creature to a given creature\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] note [NOTE_ID/TITLE] - sets a property of the creature to a given note\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] subtract [AMOUNT†] - increases a numeric property value by 1, or optionally another amount\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] -- [AMOUNT†] - alias for creature set ... subtract\n" +
                bullet + " creature set [MOBILE_ID/NAME] [KEY] ++ [AMOUNT†] - alias for creature set ... add\n" +
                "\n\r";

        // CREATURE HELP
        creatureInfo.setInfo(creatureHelp);
        creatureHandler.setDefaultAction(creatureInfo);

        // CREATURE DELETE
        DeleteCommand<Creature> deleteCommand = new DeleteCommand<>();
        deleteCommand.setService(creatureService);
        creatureHandler.getMap().put("delete", new MapSubcommand(UserRole.ROLE_STRANGER, "", deleteCommand));
        creatureHandler.getMap().put("remove", creatureHandler.getMap().get("delete"));

        // CREATURE NEW
        CreateCommand<Creature> createCommand = new CreateCommand<Creature>() {
            @Override
            public Creature buildItem(CommandContext context) throws DmFriendGeneralServiceException {
                Creature creature = new Creature();
                creature.setName(getCurrentCommandPart(context));
                creature.setOwner(context.getOwner());
                creature.setPrivacy(PrivacyType.NORMAL);
                return creature;
            }
        };
        createCommand.setPrinter(printerFactory.getCreaturePrinter());
        createCommand.setService(creatureService);
        creatureHandler.getMap().put("new", new MapSubcommand(UserRole.ROLE_STRANGER, "", createCommand));

        // CREATURE SET
        PropertySetCommand<Creature> setCommand = new PropertySetCommand<>();
        setCommand.setPrinter(printerFactory.getCreaturePrinter());
        setCommand.setService(creatureService);
        setCommand.setCreatureService(creatureService);
        setCommand.setLocationService(locationService);
        setCommand.setMobileService(mobileService);
        setCommand.setNoteService(noteService);
        creatureHandler.getMap().put("set", new MapSubcommand(UserRole.ROLE_STRANGER, "", setCommand));

        // CREATURE SHOW
        DisplayCommand<Creature> show = new DisplayCommand<>();
        show.setPrinter(printerFactory.getCreaturePrinter());
        show.setService(creatureService);
        creatureHandler.getMap().put("show", new MapSubcommand(UserRole.ROLE_STRANGER, "", show));

        // CREATURE UNSET
        PropertyDeleteCommand<Creature> unsetCommand = new PropertyDeleteCommand<>();
        unsetCommand.setService(creatureService);
        unsetCommand.setPrinter(printerFactory.getCreaturePrinter());
        creatureHandler.getMap().put("unset", new MapSubcommand(UserRole.ROLE_STRANGER, "", unsetCommand));


        return creatureHandler;
    }


    private AbstractCommand location() {
        MapCommand locationHandler = new MapCommand();
        InfoCommand locationInfo = new InfoCommand();
        String bullet = config.getInterpreterPrinterBullet();

        String locationHelp = "location help:\n" +
                config.getInterpreterPrinterHr() +
                "__Subcommands__:\n" +
                bullet + " location help - shows this message\n" +
                bullet + " location here - shows $HERE\n" +
                bullet + " location link [ORIGIN_ID/NAME] [DEST_ID/NAME] [DESCRIPTION...] - adds a link to the given destination from origin\n" +
                bullet + " location move [ID/NAME] - sets $HERE\n" +
                bullet + " location new [NAME] - creates a new location\n" +
                bullet + " location note [LOCATION_NAME/ID] [NOTE_NAME/ID]\n" +
                bullet + " location remove [ID/NAME] - deletes a location\n" +
                bullet + " location show [ID/NAME] - shows a location\n" +
                bullet + " location unlink [ORIGIN_ID/NAME] [DEST_ID/NAME] - removes the link from origin to the given location\n" +
                bullet + " location unnote [LOCATION_NAME/ID] [NOTE_NAME/ID] - removes a note from the given location\n" +
                "\n\r";

        // LOCATION HELP
        locationInfo.setInfo(locationHelp);
        locationHandler.setDefaultAction(locationInfo);

        // LOCATION HERE
        locationHandler.getMap().put("here", new MapSubcommand(UserRole.ROLE_STRANGER, "", locationHere()));

        // LOCATION LINK
        locationHandler.getMap().put("link", new MapSubcommand(UserRole.ROLE_STRANGER, "", locationLink()));

        // LOCATION MOVE
        AbstractCommand move = locationMove();
        locationHandler.getMap().put("move", new MapSubcommand(UserRole.ROLE_STRANGER, "", move));
        locationHandler.getMap().put("go", locationHandler.getMap().get("move"));

        // LOCATION NEW
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
        locationHandler.getMap().put("new", new MapSubcommand(UserRole.ROLE_STRANGER, "", create));
        locationHandler.getMap().put("+", locationHandler.getMap().get("new"));

        // LOCATION NOTE
        locationHandler.getMap().put("note", new MapSubcommand(UserRole.ROLE_STRANGER, "", locationNote()));

        // LOCATION REMOVE
        DeleteCommand<Location> remove = new DeleteCommand<>();
        remove.setService(locationService);
        locationHandler.getMap().put("remove", new MapSubcommand(UserRole.ROLE_STRANGER, "", remove));
        locationHandler.getMap().put("delete", locationHandler.getMap().get("remove"));

        // LOCATION SHOW
        DisplayCommand<Location> show = new DisplayCommand<>();
        show.setPrinter(printerFactory.getLocationPrinter());
        show.setService(locationService);
        locationHandler.getMap().put("show", new MapSubcommand(UserRole.ROLE_STRANGER, "", show));

        // LOCATION UN-LINK
        locationHandler.getMap().put("unlink", new MapSubcommand(UserRole.ROLE_STRANGER, "", locationRemoveLink()));

        // LOCATION UN-NOTE
        locationHandler.getMap().put("unnote", new MapSubcommand(UserRole.ROLE_STRANGER, "",locationRemoveNote()));

        return locationHandler;
    }

    private AbstractCommand locationHere() {
        DisplayCommand<Location> here = new DisplayCommand<Location>(){
            @Override
            public Location getItem(CommandContext context) throws DmFriendGeneralServiceException {
                Map<String, Property> globalProps = propertyService.getGlobalProperties();
                Property found = globalProps.getOrDefault(config.getLocationHereGlobalName(), null);

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
                Map<String, Property> globalProps = propertyService.getGlobalProperties();
                Property found = globalProps.getOrDefault(config.getLocationHereGlobalName(), null);

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
                    if (link.getDestination().getId().equals(child.getId())) {
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
        InfoCommand mobileInfo2 = new InfoCommand();
        String bullet = config.getInterpreterPrinterBullet();

        String mobileHelp = "mobile help: (Page 1 of 2)\n" +
                config.getInterpreterPrinterHr() +
                bullet + " mobile help - show this help page (mobile subcommands)\n" +
                bullet + " mobile help2 - show help page 2 (mobile set syntax detail)\n\r" +
                "__Subcommands__:\n" +
                bullet + " mobile blank [NAME] [HP†] [INITIATIVE†] - creates a new mobile, optionally with hp and init\n" +
                bullet + " mobile damage [ID/NAME] [AMOUNT] - reduces a mobiles HP by an amount\n" +
                bullet + " mobile delete [ID/NAME] - deletes a mobile\n" +
                bullet + " mobile heal [ID/NAME] [AMOUNT] - increases a mobiles HP by an amount\n" +
                bullet + " mobile init [ID/NAME] [NEW_INITIATIVE] - sets a mobile's initiative\n" +
                bullet + " mobile kill [ID/NAME] - insta-kill a mobile\n" +
                bullet + " mobile list - show a list of living mobiles sorted by initiative order\n" +
                bullet + " mobile list all - show a list of all mobiles, sorted by initiative order\n" +
                bullet + " mobile maxHp [ID/NAME] [NEW_MAX] - sets a mobile's max HP, doesn't heal them.\n" +
                bullet + " mobile move [ID/NAME] [NEW_POSITION] - moves a mobile to a new position\n" +
                bullet + " mobile new [CREATURE_NAME/ID] [INITIATIVE†] - creates a new mobile from a creature template, optionally with initiative\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] [...] - sets a property of the mobile, see full syntax on page 2\n" +
                bullet + " mobile show [MOBILE_ID/NAME] - displays a mobile\n" +
                bullet + " mobile restore [ID/NAME] - resets a mobile to alive, and full hp\n" +
                bullet + " mobile unset [MOBILE_ID/NAME] [KEY] - deletes a property of the mobile\n" +
                "\n\r";

        String mobileHelp2 = "mobile help: (Page 2 of 2)\n" +
                config.getInterpreterPrinterHr() +
                bullet + " mobile help - show help page 1 (mobile subcommands)\n" +
                bullet + " mobile help2 - show this help page (mobile set syntax detail)\n\r" +
                "__Mobile Set syntax__:\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] [VALUE] - sets a property of the mobile to a given value\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] add [AMOUNT†] - increases a numeric property value by 1, or optionally another amount\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] creature [CREATURE_ID/NAME] - sets a property of the mobile to a given creature\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] location [LOCATION_ID/NAME] - sets a property of the mobile to a given location\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] mobile [MOBILE_ID/NAME] - sets a property of the mobile to a given mobile\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] note [NOTE_ID/TITLE] - sets a property of the mobile to a given note\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] subtract [AMOUNT†] - increases a numeric property value by 1, or optionally another amount\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] -- [AMOUNT†] - alias for mobile set ... subtract\n" +
                bullet + " mobile set [MOBILE_ID/NAME] [KEY] ++ [AMOUNT†] - alias for mobile set ... add\n" +
                "\n\r";

        // MOBILE HELP
        mobileInfo.setInfo(mobileHelp);
        mobileHandler.setDefaultAction(mobileInfo);

        // MOBILE HELP2
        mobileInfo2.setInfo(mobileHelp2);
        mobileHandler.getMap().put("help2", new MapSubcommand(UserRole.ROLE_STRANGER, "", mobileInfo2));

        // MOBILE BLANK
        mobileHandler.getMap().put("blank", new MapSubcommand(UserRole.ROLE_STRANGER, "", mobileBlank()));

        // MOBILE DAMAGE
        AbstractCommand damage = mobileDamage();
        mobileHandler.getMap().put("damage", new MapSubcommand(UserRole.ROLE_STRANGER, "", damage));
        mobileHandler.getMap().put("dmg", mobileHandler.getMap().get("damage"));

        // MOBILE DELETE
        DeleteCommand<Mobile> delete = new DeleteCommand<>();
        delete.setService(mobileService);
        mobileHandler.getMap().put("delete", new MapSubcommand(UserRole.ROLE_STRANGER, "", delete));
        mobileHandler.getMap().put("remove", mobileHandler.getMap().get("delete"));

        // MOBILE HEAL
        mobileHandler.getMap().put("heal", new MapSubcommand(UserRole.ROLE_STRANGER, "", mobileHeal()));

        // MOBILE INIT
        UpdateCommand<Mobile> init = new UpdateCommand<Mobile>() {
            @Override
            public Mobile updateItem(CommandContext context, Mobile item) throws DmFriendGeneralServiceException {
                String argument = getCurrentCommandPart(context, 1);
                if (StringUtils.isNumeric(argument)) {
                    item.setInitiative(Integer.valueOf(argument));
                } else {
                    throw new InterpreterException("unable to parse numeric initiative in mobile_init");
                }
                return item;
            }
        };
        init.setService(mobileService);
        init.setPrinter(printerFactory.getMobilePrinter());
        mobileHandler.getMap().put("init", new MapSubcommand(UserRole.ROLE_STRANGER, "", init));

        // MOBILE KILL
        UpdateCommand<Mobile> kill = new UpdateCommand<Mobile>() {
            @Override
            public Mobile updateItem(CommandContext context, Mobile item) throws DmFriendGeneralServiceException {
                item.setHp(-1);
                item.setAlive(false);
                return item;
            }
        };
        kill.setService(mobileService);
        kill.setPrinter(printerFactory.getMobilePrinter());
        mobileHandler.getMap().put("kill", new MapSubcommand(UserRole.ROLE_STRANGER, "", kill));

        // MOBILE LIST
        DisplayCommand<Iterable<Mobile>> list = new DisplayCommand<Iterable<Mobile>>(){
            @Override
            public Iterable<Mobile> getItem(CommandContext context) throws DmFriendGeneralServiceException {
                Iterable<Mobile> mobileList = mobileService.initiativeList();

                // only show the living unless "all" is passed.
                if (!getCurrentCommandPart(context).equals("all")) {
                    Iterator<Mobile> iterator = mobileList.iterator();
                    while (iterator.hasNext()) {
                        if (!iterator.next().isAlive()) {
                            iterator.remove();
                        }
                    }
                }
                return mobileList;
            }
        };
        list.setPrinter(printerFactory.getInitiativeListPrinter());
        mobileHandler.getMap().put("list", new MapSubcommand(UserRole.ROLE_STRANGER, "", list));

        // MOBILE MAX HP
        UpdateCommand<Mobile> maxHp = new UpdateCommand<Mobile>() {
            @Override
            public Mobile updateItem(CommandContext context, Mobile item) throws DmFriendGeneralServiceException {
                String argument = getCurrentCommandPart(context, 1);
                if (StringUtils.isNumeric(argument)) {
                    item.setMaxHp(Long.valueOf(argument));

                    // don't let us overflow...
                    if (item.getMaxHp() < item.getHp()) {
                        item.setHp(item.getMaxHp());
                    }
                } else {
                    throw new InterpreterException("couldn't parse new HP");
                }

                return item;
            }
        };
        maxHp.setPrinter(printerFactory.getMobilePrinter());
        maxHp.setService(mobileService);
        mobileHandler.getMap().put("maxHp", new MapSubcommand(UserRole.ROLE_STRANGER, "", maxHp));
        mobileHandler.getMap().put("maxhp", mobileHandler.getMap().get("maxHp"));

        // MOBILE NEW
        mobileHandler.getMap().put("new", new MapSubcommand(UserRole.ROLE_STRANGER, "", mobileNew()));

        // MOBILE POS;
        UpdateCommand<Mobile> move = new UpdateCommand<Mobile>() {
            @Override
            public Mobile updateItem(CommandContext context, Mobile item) throws DmFriendGeneralServiceException {
                String position = getRemainingCommand(context);
                if (position.length() > 0) {
                    item.setPosition(position);
                } else {
                    throw new InterpreterException("new position cannot be a blank string");
                }
                return item;
            }
        };
        move.setService(mobileService);
        move.setPrinter(printerFactory.getMobilePrinter());
        mobileHandler.getMap().put("pos", new MapSubcommand(UserRole.ROLE_STRANGER, "", move));
        mobileHandler.getMap().put("position", mobileHandler.getMap().get("pos"));
        mobileHandler.getMap().put("move", mobileHandler.getMap().get("pos"));

        // MOBILE RESTORE
        UpdateCommand<Mobile> restore = new UpdateCommand<Mobile>() {
            @Override
            public Mobile updateItem(CommandContext context, Mobile mobile) throws DmFriendGeneralServiceException {
                mobile.setHp(mobile.getMaxHp());
                mobile.setAlive(true);
                return mobile;
            }
        };
        restore.setPrinter(printerFactory.getMobilePrinter());
        restore.setService(mobileService);
        mobileHandler.getMap().put("res", new MapSubcommand(UserRole.ROLE_STRANGER, "", restore));
        mobileHandler.getMap().put("restore", mobileHandler.getMap().get("res"));

        // MOBILE SET
        PropertySetCommand<Mobile> propertySet = new PropertySetCommand<>();
        propertySet.setNoteService(noteService);
        propertySet.setMobileService(mobileService);
        propertySet.setLocationService(locationService);
        propertySet.setCreatureService(creatureService);
        propertySet.setService(mobileService);
        propertySet.setPrinter(printerFactory.getMobilePrinter());
        mobileHandler.getMap().put("set", new MapSubcommand(UserRole.ROLE_STRANGER, "", propertySet));

        // MOBILE SHOW
        DisplayCommand<Mobile> show = new DisplayCommand<>();
        show.setService(mobileService);
        show.setPrinter(printerFactory.getMobilePrinter());
        mobileHandler.getMap().put("show", new MapSubcommand(UserRole.ROLE_STRANGER, "", show));

        // MOBILE UNSET
        PropertyDeleteCommand<Mobile> propertyUnset = new PropertyDeleteCommand<>();
        propertyUnset.setPrinter(printerFactory.getMobilePrinter());
        propertyUnset.setService(mobileService);
        mobileHandler.getMap().put("unset", new MapSubcommand(UserRole.ROLE_STRANGER, "", propertyUnset));

        return mobileHandler;
    }

    private AbstractCommand mobileBlank() {
        // mobile blank [NAME] [HP†] [INITIATIVE†] - creates a new mobile, optionally with hp and init
        CreateCommand<Mobile> create = new CreateCommand<Mobile>() {
            @Override
            public Mobile buildItem(CommandContext context) {
                Mobile mobile = new Mobile();

                mobile.setName(getCurrentCommandPart(context));
                mobile.setOwner(context.getOwner());
                mobile.setPrivacy(PrivacyType.NORMAL);

                String hpArg = getCurrentCommandPart(context, 1);
                if (StringUtils.isNumeric(hpArg)) {
                    mobile.setMaxHp(Long.valueOf(hpArg));
                    mobile.setHp(mobile.getMaxHp());
                }

                String initArg = getCurrentCommandPart(context, 2);
                if (StringUtils.isNumeric(initArg)) {
                    mobile.setInitiative(Integer.valueOf(initArg));
                }

                return mobile;
            }
        };
        create.setService(mobileService);
        create.setPrinter(printerFactory.getMobilePrinter());
        return create;
    }

    private AbstractCommand mobileDamage() {
        UpdateCommand<Mobile> damage = new UpdateCommand<Mobile>() {
            @Override
            public Mobile updateItem(CommandContext context, Mobile mobile) throws DmFriendGeneralServiceException {
                String argument = getCurrentCommandPart(context, 1);
                if (StringUtils.isNumeric(argument)) {
                    mobile.setHp(mobile.getHp() - Long.valueOf(argument));

                    if (mobile.getHp() < 0 ) {
                        mobile.setAlive(false);
                    }
                }

                return mobile;
            }
        };
        damage.setPrinter(printerFactory.getMobilePrinter());
        damage.setService(mobileService);

        return damage;
    }

    private AbstractCommand mobileHeal() {
        UpdateCommand<Mobile> damage = new UpdateCommand<Mobile>() {
            @Override
            public Mobile updateItem(CommandContext context, Mobile mobile) throws DmFriendGeneralServiceException {
                String argument = getCurrentCommandPart(context, 1);
                if (StringUtils.isNumeric(argument)) {
                    mobile.setHp(mobile.getHp() + Long.valueOf(argument));

                    // max hp
                    if (mobile.getHp() > mobile.getMaxHp() ) {
                        mobile.setHp(mobile.getMaxHp());
                    }

                    // life tracking
                    if (mobile.getHp() > 0) {
                        mobile.setAlive(true);
                    }
                }

                return mobile;
            }
        };
        damage.setPrinter(printerFactory.getMobilePrinter());
        damage.setService(mobileService);

        return damage;
    }

    private AbstractCommand mobileNew() {
        CreateCommand<Mobile> convert = new CreateCommand<Mobile>() {
            @Override
            public Mobile buildItem(CommandContext context) throws DmFriendGeneralServiceException {
                Creature creature = creatureService.read(getCurrentCommandPart(context));
                Mobile mobile = mobileService.fromCreature(creature);
                return mobile;
            }
            @Override
            public Mobile save(Mobile mobile) throws DmFriendGeneralServiceException {
                return service.update(mobile);
            }
        };
        convert.setService(mobileService);
        convert.setPrinter(printerFactory.getMobilePrinter());
        return convert;
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
        noteHandler.getMap().put("append", new MapSubcommand(UserRole.ROLE_STRANGER, "", append));
        noteHandler.getMap().put("++", noteHandler.getMap().get("append"));

        // NOTE LIST
        DisplayCommand<Iterable<Note>> list = new DisplayCommand<Iterable<Note>>() {
            @Override
            public List<Note> getItem(CommandContext context) throws NoteException {
                return noteService.getGlobalNotes();
            }
        };
        list.setPrinter(printerFactory.getNoteListPrinter());
        noteHandler.getMap().put("list", new MapSubcommand(UserRole.ROLE_STRANGER, "", list));

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
            public void afterSave(CommandContext context, Note note) throws NoteException {
                noteService.attachToGlobalContext(note);
            }
        };
        create.setService(noteService);
        create.setPrinter(printerFactory.getNotePrinter());
        noteHandler.getMap().put("new", new MapSubcommand(UserRole.ROLE_STRANGER, "", create));
        noteHandler.getMap().put("+", noteHandler.getMap().get("new"));

        // NOTE REMOVE
        DeleteCommand<Note> remove = new DeleteCommand<>();
        remove.setService(noteService);
        noteHandler.getMap().put("delete", new MapSubcommand(UserRole.ROLE_STRANGER, "", remove));
        noteHandler.getMap().put("remove", noteHandler.getMap().get("delete"));

        // NOTE SET
        UpdateCommand<Note> set = new UpdateCommand<Note>() {
            @Override
            public Note updateItem(CommandContext context, Note item) {
                String value = getRemainingCommand(context).concat("\n");
                item.setBody(value);

                return item;
            }
        };
        set.setPrinter(printerFactory.getNotePrinter());
        set.setService(noteService);
        noteHandler.getMap().put("set", new MapSubcommand(UserRole.ROLE_STRANGER, "", set));

        // NOTE SHOW
        DisplayCommand<Note> show = new DisplayCommand<>();
        show.setPrinter(printerFactory.getNotePrinter());
        show.setService(noteService);
        noteHandler.getMap().put("show", new MapSubcommand(UserRole.ROLE_STRANGER, "", show));

        // return
        return noteHandler;
    }

    private AbstractCommand ping() {
        InfoCommand pingHandler = new InfoCommand();
        pingHandler.setInfo("Pong!");

        return pingHandler;
    }


    private AbstractCommand turn() {
        MapCommand turnHandler = new MapCommand();
        InfoCommand turnInfo = new InfoCommand();
        String bullet = config.getInterpreterPrinterBullet();

        String turnHelp = "turn help:\n" +
                config.getInterpreterPrinterHr() +
                "__Subcommands__:\n" +
                bullet + " turn done - end the current turn immediately\n" +
                bullet + " turn help - show this help message\n" +
                bullet + " turn next - move to the next mobile's turn, making them $ACTIVE\n" +
                bullet + " turn show - show the $ACTIVE mobile\n" +
                "\n\r";

        // TURN HELP
        turnInfo.setInfo(turnHelp);
        turnHandler.setDefaultAction(turnInfo);

        // TURN DONE
        AbstractCommand done = new AbstractCommand() {
            @Override
            public void handle(CommandContext context) throws InterpreterException {
                try {
                    Property activeProperty = propertyService.getGlobalProperties()
                            .getOrDefault(config.getMobileActiveGlobalName(), null);

                    if (activeProperty != null) {
                        propertyService.detachFromGlobalContext(activeProperty);
                        propertyService.delete(activeProperty);

                        context.setResponse("OK");
                        return;
                    }
                    context.setResponse("Already there, boss.");

                } catch (PropertyException ex) {
                    throw new InterpreterException("failed to delete $ACTIVE: ".concat(ex.getMessage()), ex);
                }
            }
        };
        turnHandler.getMap().put("done", new MapSubcommand(UserRole.ROLE_STRANGER, "", done));

        // TURN NEXT
        turnHandler.getMap().put("next", new MapSubcommand(UserRole.ROLE_STRANGER, "", turnNext()));

        // TURN SHOW
        turnHandler.getMap().put("show", new MapSubcommand(UserRole.ROLE_STRANGER, "", turnShow()));

        return turnHandler;
    }

    private AbstractCommand turnNext() {
        return new AbstractCommand() {
            @Override
            public void handle(CommandContext context) throws InterpreterException {

                try {
                    int activeInit = 0;
                    String activeName = "";
                    Property activeProperty = propertyService.getGlobalProperties()
                            .getOrDefault(config.getMobileActiveGlobalName(), null);

                    // figure out active name, init
                    if (activeProperty == null) {
                        // construct new $ACTIVE property
                        activeProperty = new Property();
                        activeProperty.setType(PropertyType.STRING);
                        activeProperty.setOwner(config.getSystemGroupOwner());
                        activeProperty.setName(config.getMobileActiveGlobalName());
                        activeProperty.setPrivacy(PrivacyType.NORMAL);
                    } else {
                        // parse the existing values out of $ACTIVE
                        int indexOfDelimiter = activeProperty.getValue().indexOf('|');
                        if (indexOfDelimiter > 0) {
                            activeInit = Integer.valueOf(activeProperty.getValue().substring(0,indexOfDelimiter));
                            activeName = activeProperty.getValue().substring(indexOfDelimiter + 1);
                        }
                    }

                    for (Mobile mobile : mobileService.initiativeList()) {
                        // don't disturb the dead.
                        if (!mobile.isAlive()) {
                            continue;
                        }

                        if (
                                // either; this is our new next because it's the first alive we found over last init;
                                (mobile.getInitiative() > activeInit) ||
                                // or: this is our new next because its tied for init, and string compares higher than saved val
                                (mobile.getInitiative() == activeInit && activeName.compareToIgnoreCase(mobile.getName()) < 0)) {

                            // then we found what we were looking for; save, reply, and return
                            saveAndRespond(context, mobile, activeProperty);
                            return;
                        }
                    }

                    // got to the end of the list...
                    context.setResponse("**END of TURN**\n\r");
                    if (activeProperty.getId() != null) {
                        propertyService.detachFromGlobalContext(activeProperty);
                        propertyService.delete(activeProperty);
                    }

                    return;

                } catch (DmFriendGeneralServiceException ex) {
                    throw new InterpreterException("Failed to increment $ACTIVE: ".concat(ex.getMessage()), ex);
                }
            }

            private void saveAndRespond(CommandContext context, Mobile mob, Property property) throws PropertyException {
                PrettyPrinter<Mobile> printer = printerFactory.getMobilePrinter();
                property.setValue(String.format("%d|%s", mob.getInitiative(), mob.getName()));

                if (property.getId() == null) {
                    Property savedProperty = propertyService.create(property);
                    propertyService.attachToGlobalContext(savedProperty);

                    context.setResponse(printer.print(mob));
                } else {
                    context.setResponse(printer.print(mob));
                    propertyService.update(property);
                }
            }
        };
    }

    private AbstractCommand turnShow() {
        DisplayCommand<Mobile> show = new DisplayCommand<Mobile>(){
            @Override
            public Mobile getItem(CommandContext context) throws DmFriendGeneralServiceException {

                String activeName = "";
                Property activeProperty = propertyService.getGlobalProperties()
                        .getOrDefault(config.getMobileActiveGlobalName(), null);

                if (activeProperty == null) {
                    throw new InterpreterException("No active turn.");
                }

                int indexOfDelimiter = activeProperty.getValue().indexOf('|');
                if (indexOfDelimiter > 0) {
                    activeName = activeProperty.getValue().substring(indexOfDelimiter + 1);
                }

                if (mobileService.exists(activeName)) {
                    return mobileService.read(activeName);
                } else {
                    throw new InterpreterException("Can't find $ACTIVE mobile");
                }
            }
        };
        show.setPrinter(printerFactory.getMobilePrinter());
        return show;
    }


    private AbstractCommand var() {
        MapCommand varHandler = new MapCommand();
        InfoCommand varInfo = new InfoCommand();
        String bullet = config.getInterpreterPrinterBullet();

        String varHelp = "var help:\n" +
                config.getInterpreterPrinterHr() +
                "__Subcommands__:\n" +
                bullet + " var delete [KEY] - remove a variable\n" +
                bullet + " var list - prints out all variables\n" +
                bullet + " var set [KEY] [VALUE] - sets a variable to a given value\n" +
                bullet + " var set [KEY] add [AMOUNT†] - increases a numeric variable value by 1, or optionally another amount\n" +
                bullet + " var set [KEY] creature [CREATURE_ID/NAME] - sets a variable to a given creature\n" +
                bullet + " var set [KEY] location [LOCATION_ID/NAME] - sets a variable to a given location\n" +
                bullet + " var set [KEY] mobile [MOBILE_ID/NAME] - sets a variable to a given mobile\n" +
                bullet + " var set [KEY] note [NOTE_ID/TITLE] - sets a variable to a given note\n" +
                bullet + " var set [KEY] subtract [AMOUNT†] - increases a numeric variable value by 1, or optionally another amount\n" +
                bullet + " var set [KEY] -- [AMOUNT†] - alias for var set ... subtract\n" +
                bullet + " var set [KEY] ++ [AMOUNT†] - alias for var set ... add\n" +
                "\n\r";

        // VAR HELP
        varInfo.setInfo(varHelp);
        varHandler.setDefaultAction(varInfo);

        // VAR DELETE
        AbstractCommand delete = new AbstractCommand() {
            @Override
            public void handle(CommandContext context) throws InterpreterException {
                String key = getCurrentCommandPart(context);

                try {
                    Map<String, Property> globalProperties = propertyService.getGlobalProperties();
                    if (globalProperties.containsKey(key)) {
                        propertyService.detachFromGlobalContext(globalProperties.get(key));
                        propertyService.delete(globalProperties.get(key));
                        context.setResponse("OK");
                    } else {
                        context.setResponse("Not Found!");
                    }
                } catch(DmFriendGeneralServiceException ex) {
                    throw new InterpreterException("Var Delete: Bad happened: "+ ex.getMessage(), ex);
                }
            }
        };
        varHandler.getMap().put("delete", new MapSubcommand(UserRole.ROLE_STRANGER, "", delete));
        varHandler.getMap().put("remove", varHandler.getMap().get("delete"));

        // VAR LIST
        DisplayCommand<Map<String,Property>> list = new DisplayCommand<Map<String, Property>>() {
            @Override
            public Map<String, Property> getItem(CommandContext context) throws DmFriendGeneralServiceException {
                return propertyService.getGlobalProperties();
            }
        };
        list.setPrinter(printerFactory.getPropertyMapPrinter());
        varHandler.getMap().put("list", new MapSubcommand(UserRole.ROLE_STRANGER, "", list));

        // VAR SET
        GlobalPropertySetCommand set = new GlobalPropertySetCommand();
        set.setPropertyPrinter(printerFactory.getPropertyPrinter());
        set.setPropertyService(propertyService);
        set.setCreatureService(creatureService);
        set.setLocationService(locationService);
        set.setMobileService(mobileService);
        set.setNoteService(noteService);
        varHandler.getMap().put("set", new MapSubcommand(UserRole.ROLE_STRANGER, "", set));

        return varHandler;
    }


    private AbstractCommand user() {
        MapCommand userHandler = new MapCommand();
        AbstractCommand userInfo = userHandler.getAutoHelpCommand();

        UserGrantCommand grant = new UserGrantCommand();
        grant.setUserService(userService);
        userHandler.getMap().put("grant", new MapSubcommand(UserRole.ROLE_ASSISTANT, "[USER] [ROLE] [GAME(Optional)] - Sets a user to the given role", grant));

        UserRevokeCommand revoke = new UserRevokeCommand();
        revoke.setUserService(userService);
        userHandler.getMap().put("revoke", new MapSubcommand(UserRole.ROLE_GAME_MASTER, "[USER] [GAME(Optional)] - removes a user", revoke));

        userHandler.setDefaultAction(userInfo);

        /* aliases
        userHandler.getMap().put("add", userHandler.getMap().get("grant"));
        userHandler.getMap().put("give", userHandler.getMap().get("grant"));
        userHandler.getMap().put("set", userHandler.getMap().get("grant"));

        userHandler.getMap().put("ban", userHandler.getMap().get("revoke"));
        userHandler.getMap().put("rm", userHandler.getMap().get("revoke"));
        userHandler.getMap().put("remove", userHandler.getMap().get("revoke"));
        */

        return userHandler;
    }
}
