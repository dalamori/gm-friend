package net.dalamori.GMFriend.models.enums;

public enum UserRole {
    // Roles are given in order from highest to lowest, each role includes perms of all roles under it.
    ROLE_SUPER_USER,    // all perms everywhere
    ROLE_GAME_MASTER,   // can create games(aka tenants), edit all objects within a game
    ROLE_ASSISTANT,     // can create, edit and delete all objects, and heal/damage/turn while not $ACTIVE, can echo shows
    ROLE_AUTHOR,        // can create new objects which will be then be owned.
    ROLE_OWNER,         // can edit and delete owned objects.
    ROLE_PLAYER,        // can possess a mob, and can heal, damage, turn next when that mobile is $ACTIVE
    ROLE_OBSERVER,      // can show game objects privately
    ROLE_STRANGER       // confers no privileges
}
