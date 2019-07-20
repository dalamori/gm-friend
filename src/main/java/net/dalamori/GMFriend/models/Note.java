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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Data
@Entity
@Table(name = "NOTES")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Lob
    @NotBlank
    @Column(nullable = false, name = "BODY")
    @Size(max = 1850)
    private String body;

    @NotBlank
    @Column(nullable = false, unique = true, name = "TITLE")
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "PRIVACY_TYPE")
    private PrivacyType privacy;

    @NotBlank
    @Column(nullable = false, name = "OWNER")
    private String owner;
}
