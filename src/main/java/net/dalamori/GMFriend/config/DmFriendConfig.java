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

    @Value("${creatures.properties.maxHpName:maxHp}")
    private String creaturePropertyMaxHpName;

    @Value("${groups.system.collisionPrefix:!!LOST+FOUND=}")
    private String systemGroupCollisionPrefix;

    @Value("${groups.system.creaturePropertyAction:Properties for Creature #}")
    private String systemGroupCreaturePropertyAction;

    @Value("${groups.system.globalNoteAction:Global Notes}")
    private String systemGroupGlobalNoteAction;

    @Value("${groups.system.globalPropertiesAction:Global Variables}")
    private String systemGroupGlobalVarsAction;

    @Value("${groups.system.locationNoteAction:Notes for Loc #}")
    private String systemGroupLocationNoteAction;

    @Value("${groups.system.mobilePropertyAction:Properties for Mobile #}")
    private String systemGroupMobilePropertyAction;

    @Value("${groups.system.owner:__Internal__}")
    private String systemGroupOwner;

    @Value("${groups.system.prefix:__SYS__}")
    private String systemGroupPrefix;

    @Value("${interpreter.commandPrefix:;;}")
    private String interpreterCommandPrefix;

    @Value("${interpreter.printer.bullet:▸}")
    private String interpreterPrinterBullet;

    @Value("${interpreter.printer.emphasisBullet:★}")
    private String interpreterPrinterEmphasisBullet;

    @Value("${interpreter.printer.hr:-=-=-=-=-=-=-=-=-=-\n}")
    private String interpreterPrinterHr;

    @Value("${location.here.globalName:__$HERE__}")
    private String locationHereGlobalName;

    @Value("${mobiles.name.maxRetries:50}")
    private int mobileNameMaxRetries;

    @Value("${mobiles.active.globalName:__$ACTIVE__}")
    private String mobileActiveGlobalName;
}


