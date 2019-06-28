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
import javax.persistence.Lob;
import javax.persistence.Table;


@Data
@Entity
@Table(name = "NOTES")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Lob
    @Column(nullable = false, name = "BODY")
    private String body;

    @Column(nullable = false, unique = true, name = "TITLE")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "PRIVACY_TYPE")
    private PrivacyType privacy;

    @Column(nullable = false, name = "OWNER")
    private String owner;
}
