package net.dalamori.GMFriend.testing;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.Location;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;

public class TestDataFactory {

    public static String OWNER_NAME = "Spiderman";
    public static Long DEFAULT_ID = null;
    public static String DEFAULT_NAME = "Default";

    public static Group makeGroup(Long id, String name) {
        Group group = new Group();
        Long contentId = Long.valueOf(321);

        group.setPrivacy(PrivacyType.NORMAL);
        group.setOwner(OWNER_NAME);
        group.setId(id);
        group.setName(name);
        group.getContents().add(contentId);
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

}
