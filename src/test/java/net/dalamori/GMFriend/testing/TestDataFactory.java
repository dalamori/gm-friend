package net.dalamori.GMFriend.testing;

import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.Note;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;

public class TestDataFactory {

    public static String OWNER_NAME = "Spiderman";
    public static Long DEFAULT_ID = null;
    public static String DEFAULT_NAME = "Default";
    public static final String LIPSUM_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
            + "Nunc eget metus consequat orci blandit aliquet. Cras in porttitor arcu. Suspendisse interdum ultrices "
            + "dui eu tempor. Pellentesque id auctor est, at malesuada magna. Ut dignissim elit sit amet tempus "
            + "imperdiet. Phasellus consequat dignissim tortor, eu eleifend sapien pulvinar at. Quisque lacinia dui "
            + "eget lectus pharetra, ut ullamcorper tellus finibus. Donec at pharetra nunc, eget feugiat elit.";

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

    public static Location makeLocation(Long id, String name) {
        Location location = new Location();

        location.setId(id);
        location.setPrivacy(PrivacyType.NORMAL);
        location.setOwner(OWNER_NAME);
        location.setName(name);

        return location;
    }

    public static Location makeLocation() {
        return makeLocation(DEFAULT_ID, DEFAULT_NAME);
    }

    public static Note makeNote(Long id, String title) {
        Note note = new Note();

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
}
