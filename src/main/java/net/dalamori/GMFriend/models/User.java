package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.UserRole;

import java.util.HashMap;
import java.util.Map;

@Data
public class User {

    private String owner;

    private UserRole baseRole = UserRole.ROLE_STRANGER;

    private Map<String, UserRole> gameRoles = new HashMap<>();

}
