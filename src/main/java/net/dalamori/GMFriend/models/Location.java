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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Entity
@Table(name = "LOCATIONS")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, name = "NAME")
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "PRIVACY_TYPE")
    private PrivacyType privacy;

    @NotBlank
    @Column(name = "OWNER", nullable = false)
    private String owner;


}
