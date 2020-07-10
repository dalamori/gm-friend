package net.dalamori.GMFriend.models.enums;

public enum UserRole {

    // Roles are given in order from highest to lowest, each role includes perms of all roles under it.
    ROLE_STRANGER,       // confers no privileges
    ROLE_OBSERVER,      // can show game objects privately
    ROLE_PLAYER,        // can possess a mob, and can heal, damage, turn next when that mobile is $ACTIVE
    ROLE_OWNER,         // can edit owned objects.
    ROLE_AUTHOR,        // can create and delete new objects which will be then be owned.
    ROLE_ASSISTANT,     // can create, edit and delete unowned objects within game, and heal/damage/turn while not $ACTIVE, can echo shows
    ROLE_GAME_MASTER,   // can create games, assign perms within a game
    ROLE_SUPER_USER;    // all perms everywhere

    public static UserRole fromInputString(String val) {

        switch (val) {
            case "super":
            case "root":
            case "admin":
                return UserRole.ROLE_SUPER_USER;

            case "gm":
            case "master":
            case "gamemaster":
                return UserRole.ROLE_GAME_MASTER;

            case "assistant":
            case "gma":
            case "assist":
                return UserRole.ROLE_ASSISTANT;

            case "author":
                return UserRole.ROLE_AUTHOR;

            case "owner":
                return UserRole.ROLE_OWNER;

            case "player":
                return UserRole.ROLE_PLAYER;

            case "observer":
                return UserRole.ROLE_OBSERVER;

            case "stranger":
            default:
                return UserRole.ROLE_STRANGER;
        }
    }
}
