package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.PrivacyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "MOBILES")
@Data
public class Mobile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRIVACY_TYPE", nullable = false)
    private PrivacyType privacy;

    @Column(name = "OWNER", nullable = false)
    private String owner;

    @Column(name = "CREATURE_ID")
    private Long creatureId;

    @Column(name = "MAX_HP", nullable = false)
    private long maxHp;

    @Column(name = "HP", nullable = false)
    private long hp;

    @Column(name = "INITIATIVE", nullable = false)
    private int initiative;

    @Column(name = "ALIVE", nullable = false)
    private boolean alive;

    @Column(name = "POSITION", nullable = false)
    private String position;

    @Transient
    private Map<String, Property> propertyMap = new HashMap<>();
}
