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

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "GROUP_CONTENTS", joinColumns = @JoinColumn(name = "GROUP_ID"))
    @Column(name="CONTENT_ID")
    private Set<Long> contents = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "PRIVACY_TYPE")
    private PrivacyType privacy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "CONTENT_TYPE")
    private PropertyType contentType;

    @Column(name = "OWNER")
    private String owner;
}
