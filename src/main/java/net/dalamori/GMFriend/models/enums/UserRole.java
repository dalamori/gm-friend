package net.dalamori.GMFriend.models.enums;

public enum UserRole {
    // Roles are given in order from highest to lowest, each role includes perms of all roles under it.
    ROLE_SUPER_USER,    // all perms everywhere
    ROLE_GAME_MASTER,   // can create games, assign perms within a game
    ROLE_ASSISTANT,     // can create, edit and delete unowned objects within game, and heal/damage/turn while not $ACTIVE, can echo shows
    ROLE_AUTHOR,        // can create and delete new objects which will be then be owned.
    ROLE_OWNER,         // can edit owned objects.
    ROLE_PLAYER,        // can possess a mob, and can heal, damage, turn next when that mobile is $ACTIVE
    ROLE_OBSERVER,      // can show game objects privately
    ROLE_STRANGER       // confers no privileges
}
