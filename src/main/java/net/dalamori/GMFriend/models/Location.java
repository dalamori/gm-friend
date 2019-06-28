package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.PrivacyType;

import java.util.Set;

@Data
public class Location {
    private Long id;
    private String name;
    private Set<Note> notes;
    private PrivacyType privacy;
}
