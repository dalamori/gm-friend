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
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@Table(name = "CREATURES")
public class Creature implements HasOwner, HasProperties {
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

    @Valid
    @Transient
    private Map<String, Property> propertyMap = new HashMap<>();

}
