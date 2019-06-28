package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.PrivacyType;

import java.util.Set;

@Data
public class LocationLink {
    private Long id;
    private String description;
    private Set<Location> locations;
    private PrivacyType privacy;
}
