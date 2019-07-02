package net.dalamori.GMFriend.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Data
@Configuration
@PropertySources(value = {
        @PropertySource(value = "classpath:application.properties"),
        @PropertySource(value = "classpath:interpreter.properties", ignoreResourceNotFound = true)
})
public class DmFriendConfig {

    @Value("${groups.system.prefix:__SYS__}")
    private String systemGroupPrefix;

    @Value("${groups.system.owner:__Internal__}")
    private String systemGroupOwner;

    @Value("${groups.system.collisionPrefix:__LOST+FOUND__}")
    private String systemGroupCollisionPrefix;

    @Value("${groups.system.locationNoteAction:Notes for Loc #}")
    private String systemGroupLocationNoteAction;

    @Value("${groups.system.globalNoteAction:Global Notes}")
    private String systemGroupGlobalNoteAction;

    @Value("${groups.system.globalPropertiesAction:Global Variables}")
    private String systemGroupGlobalVarsAction;

    @Value("${groups.system.creaturePropertyAction:Properties for Creature #}")
    private String systemGroupCreaturePropertyAction;

    @Value("${groups.system.mobilePropertyAction:Properties for Mobile #}")
    private String systemGroupMobilePropertyAction;

}
