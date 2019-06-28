package net.dalamori.GMFriend.models;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@Entity
public class Creature {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany
    @JoinTable
    private Set<Property> properties;

    @Transient
    private Map<String, Property> propertyMap;

    public Map<String, Property> getPropertyMap() {
        if (propertyMap == null) {
            propertyMap = new HashMap<>();

            for (Property property : properties) {
                propertyMap.put(property.getName(), property);
            }
        }

        return propertyMap;
    }
}
