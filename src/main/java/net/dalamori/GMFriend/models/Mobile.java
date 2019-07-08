package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.interfaces.HasOwner;
import net.dalamori.GMFriend.models.interfaces.HasProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "MOBILES")
@Data
public class Mobile implements HasOwner, HasProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotBlank
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "PRIVACY_TYPE", nullable = false)
    private PrivacyType privacy;

    @NotBlank
    @Column(name = "OWNER", nullable = false)
    private String owner;

    @Column(name = "CREATURE_ID")
    private Long creatureId;

    @Positive
    @Column(name = "MAX_HP", nullable = false)
    private long maxHp = 1;

    @Column(name = "HP", nullable = false)
    private long hp = 1;

    @PositiveOrZero
    @Column(name = "INITIATIVE", nullable = false)
    private int initiative =  9999;

    @Column(name = "ALIVE", nullable = false)
    private boolean alive = true;

    @NotBlank
    @Column(name = "POSITION", nullable = false)
    private String position = "here";

    @Valid
    @Transient
    private Map<String, Property> propertyMap = new HashMap<>();
}
