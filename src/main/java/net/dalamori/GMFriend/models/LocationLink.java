package net.dalamori.GMFriend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "LOCATION_LINKS")
public class LocationLink {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(targetEntity = Location.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "DEST")
    @JsonIgnoreProperties({"links", "notes"})
    private Location destination;

    @ManyToOne(targetEntity = Location.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "ORIGIN")
    @JsonIgnoreProperties({"links", "notes"})
    private Location origin;

    @Column(name = "SHORT_DESC", nullable = false)
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "PRIVACY_TYPE", nullable = false)
    private PrivacyType privacy;
}
