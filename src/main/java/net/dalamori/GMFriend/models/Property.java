package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Data
@Entity
@Table(name = "PROPERTIES")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, name = "ID")
    private Long id;

    @Column(nullable = false, name = "NAME")
    private String name;

    @Column(nullable = false, name = "VALUE")
    private String value;  // may be name of resource, or literal value, depending on type

    @Transient
    private int turns;

    @Transient
    private boolean alarm;

    @Transient
    private int listOffset;  // side-stepping issues of iterator type

    @Column(nullable = false, name = "PROPERTY_TYPE")
    @Enumerated(EnumType.STRING)
    private PropertyType type;

    @Column(nullable = false, name = "PRIVACY_TYPE")
    @Enumerated(EnumType.STRING)
    private PrivacyType privacy;

    @Column(nullable = true, name = "OWNER")
    private String owner;

}
