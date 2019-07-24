package net.dalamori.GMFriend.testing;

import net.dalamori.GMFriend.interpreter.CommandContext;
import net.dalamori.GMFriend.models.Creature;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.LocationLink;
import net.dalamori.GMFriend.models.Mobile;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.Property;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;

import java.security.PublicKey;
import java.util.Arrays;

public class TestDataFactory {

    public static String OWNER_NAME = "Spiderman";
    public static Long DEFAULT_ID = null;
    public static String DEFAULT_NAME = "Default";
    public static final String LIPSUM_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
            + "Nunc eget metus consequat orci blandit aliquet. Cras in porttitor arcu. Suspendisse interdum ultrices "
            + "dui eu tempor. Pellentesque id auctor est, at malesuada magna. Ut dignissim elit sit amet tempus "
            + "imperdiet. Phasellus consequat dignissim tortor, eu eleifend sapien pulvinar at. Quisque lacinia dui "
            + "eget lectus pharetra, ut ullamcorper tellus finibus. Donec at pharetra nunc, eget feugiat elit.";

    public static CommandContext makeContextFromCommandLine(String commandLine) {
        CommandContext context = new CommandContext();
        context.setOwner(OWNER_NAME);
        context.setIndex(0);
        context.setCommand(Arrays.asList(commandLine.split("\\s")));

        return context;
    }

    // Creatures
    public static Creature makeCreature(Long id, String name) {
        Creature creature = new Creature();

        creature.setId(id);
        creature.setName(name);
        creature.setOwner(OWNER_NAME);
        creature.setPrivacy(PrivacyType.NORMAL);

        return creature;
    }

    public static Creature makeCreature(String name) {
        return makeCreature(DEFAULT_ID, name);
    }

    public static Creature makeCreature() {
        return makeCreature(DEFAULT_ID, DEFAULT_NAME);
    }

    // Groups
    public static Group makeGroup(Long id, String name) {
        Group group = new Group();
        Long contentId = Long.valueOf(321);

        group.setPrivacy(PrivacyType.NORMAL);
        group.setOwner(OWNER_NAME);
        group.setId(id);
        group.setName(name);
        group.setContentType(PropertyType.NOTE);

        return group;
    }

    public static Group makeGroup() {
        return makeGroup(DEFAULT_ID, DEFAULT_NAME);
    }

    public static Group makeGroup(String name) {
        return makeGroup(DEFAULT_ID, name);
    }

    // Locations
    public static Location makeLocation(Long id, String name) {
        Location location = new Location();

        location.setId(id);
        location.setPrivacy(PrivacyType.NORMAL);
        location.setOwner(OWNER_NAME);
        location.setName(name);

        return location;
    }

    public static Location makeLocation(String name) {
        return makeLocation(DEFAULT_ID, name);
    }

    public static Location makeLocation() {
        return makeLocation(DEFAULT_ID, DEFAULT_NAME);
    }

    // LocationLinks
    public static LocationLink makeLink(String name, Location origin, Location dest) {
        LocationLink link = new LocationLink();
        link.setShortDescription(name);
        link.setOrigin(origin);
        link.setDestination(dest);
        link.setPrivacy(PrivacyType.NORMAL);

        return link;
    }

    public static LocationLink makeLink(Location origin, Location dest) {
        return makeLink(DEFAULT_NAME, origin, dest);
    }

    public static LocationLink makeLink() {
        return makeLink(DEFAULT_NAME, null, null);
    }

    // Mobiles
    public static Mobile makeMobile(Long id, String name) {
        Mobile mobile = new Mobile();

        mobile.setId(id);
        mobile.setName(name);
        mobile.setOwner(OWNER_NAME);
        mobile.setMaxHp(100L);
        mobile.setHp(50L);
        mobile.setPosition("nearby");
        mobile.setPrivacy(PrivacyType.NORMAL);
        mobile.setInitiative(13);

        return mobile;
    }

    public static Mobile makeMobile(String name) {
        return makeMobile(DEFAULT_ID, name);
    }

    public static Mobile makeMobile() {
        return makeMobile(DEFAULT_ID, DEFAULT_NAME);
    }

    // Notes
    public static Note makeNote(Long id, String title) {
        Note note = new Note();

        note.setId(id);
        note.setPrivacy(PrivacyType.NORMAL);
        note.setOwner(OWNER_NAME);
        note.setBody(LIPSUM_TEXT);
        note.setTitle(title);

        return note;
    }

    public static Note makeNote(String title) {
        return makeNote(DEFAULT_ID, title);
    }

    public static Note makeNote() {
        return makeNote(DEFAULT_ID, DEFAULT_NAME);
    }

    // Properties
    public static Property makeProperty(Long id, String name) {
        Property property = new Property();

        property.setId(id);
        property.setName(name);
        property.setOwner(OWNER_NAME);
        property.setType(PropertyType.STRING);
        property.setPrivacy(PrivacyType.NORMAL);
        property.setValue("Secret word Azschalfrazz");

        return property;
    }

    public static Property makeProperty(String name) {
        return makeProperty(DEFAULT_ID, name);
    }

    public static Property makeProperty() {
        return makeProperty(DEFAULT_ID, DEFAULT_NAME);
    }
}
