package net.dalamori.GMFriend.models;

import lombok.Data;
import net.dalamori.GMFriend.models.enums.PrivacyType;
import net.dalamori.GMFriend.models.enums.PropertyType;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "GROUP_LISTS")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @NotBlank
    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "GROUP_CONTENTS", joinColumns = @JoinColumn(name = "GROUP_ID"))
    @Column(name="CONTENT_ID")
    private Set<Long> contents = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "PRIVACY_TYPE")
    private PrivacyType privacy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "CONTENT_TYPE")
    private PropertyType contentType;

    @NotBlank
    @Column(name = "OWNER")
    private String owner;
}
