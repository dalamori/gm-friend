package net.dalamori.GMFriend.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dalamori.GMFriend.models.enums.PrivacyType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "LOCATIONS")
@EqualsAndHashCode(exclude = {"notes", "links"})
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(nullable = false, unique = true, name = "NAME")
    private String name;

    @ManyToMany(targetEntity = Note.class, fetch = FetchType.EAGER)
    @JoinTable(name = "LOCATION_NOTES",
            joinColumns = @JoinColumn(name = "NOTE_ID"),
            inverseJoinColumns = @JoinColumn(name = "LOCATION_ID")
    )
    private Set<Note> notes = new HashSet<>();

    @OneToMany(targetEntity = LocationLink.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "ORIGIN")
    private Set<LocationLink> links = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "PRIVACY_TYPE")
    private PrivacyType privacy;

    @Column(name = "OWNER", nullable = false)
    private String owner;
}
