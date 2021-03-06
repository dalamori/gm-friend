package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;
import net.dalamori.GMFriend.models.interfaces.HasOwner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "PROPERTIES")
public class Property implements HasOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, name = "ID")
    private Long id;

    @NotBlank
    @Column(nullable = false, name = "NAME")
    private String name;

    @NotBlank
    @Column(nullable = false, name = "VALUE")
    private String value;  // may be name of resource, or literal value, depending on type

    @NotNull
    @Column(nullable = false, name = "PROPERTY_TYPE")
    @Enumerated(EnumType.STRING)
    private PropertyType type;

    @NotNull
    @Column(nullable = false, name = "PRIVACY_TYPE")
    @Enumerated(EnumType.STRING)
    private PrivacyType privacy;

    @NotBlank
    @Column(nullable = false, name = "OWNER")
    private String owner;

}
