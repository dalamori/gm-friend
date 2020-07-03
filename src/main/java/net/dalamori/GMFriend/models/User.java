package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.UserRole;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(
    name = "USERS",
    indexes = {
        @Index(name = "owner_idx", columnList = ("OWNER")),
        @Index(name = "game_idx", columnList = ("GAME")) },
    uniqueConstraints = {
        @UniqueConstraint(name = "owner_game_unique", columnNames = {"OWNER", "GAME"})
    })
public class User {
    public static final String GLOBAL_GAME_ID = "GLOBAL_GAME_MAGIC_3945c255-55c4-4256-92f2-114136e6a651";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, name = "ID")
    private Long id;

    @NotBlank
    @Column(nullable = false, name = "OWNER")
    private String owner;

    @NotNull
    @Column(nullable = false, name = "ROLE")
    @Enumerated(EnumType.ORDINAL)
    private UserRole role = UserRole.ROLE_STRANGER;

    @NotBlank
    @Column(nullable = false, name = "GAME")
    private String game;

}
