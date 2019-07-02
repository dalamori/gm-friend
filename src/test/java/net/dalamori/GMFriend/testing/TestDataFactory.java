package net.dalamori.GMFriend.testing;

import net.dalamori.GMFriend.config.DmFriendConfig;
import net.dalamori.GMFriend.models.Group;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;

public class TestDataFactory {

    public static DmFriendConfig config;

    public static Group makeGroup(Long id, String name) {
        Group group = new Group();
        Long contentId = Long.valueOf(321);

        group.setPrivacy(PrivacyType.INTERNAL);
        group.setOwner(config.getSystemGroupOwner());
        group.setId(id);
        group.setName(name);
        group.getContents().add(contentId);
        group.setContentType(PropertyType.NOTE);

        return group;
    }

    public static Group makeGroup() {
        return makeGroup(Long.valueOf(123), "defaultName");
    }

    public static Group makeGroup(String name) {
        return makeGroup(Long.valueOf(123), name);
    }

}
