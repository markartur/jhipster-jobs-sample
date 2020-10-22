package com.jhipsterdemo.company.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.DBRef;

import org.springframework.data.elasticsearch.annotations.FieldType;
import java.io.Serializable;

/**
 * A Accommodation.
 */
@Document(collection = "accommodation")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "accommodation")
public class Accommodation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("name")
    private String name;

    @Field("hotelier")
    private String hotelier;

    @Field("category")
    private String category;

    @DBRef
    @Field("location")
    @JsonIgnoreProperties(value = "accommodations", allowSetters = true)
    private Location location;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Accommodation name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHotelier() {
        return hotelier;
    }

    public Accommodation hotelier(String hotelier) {
        this.hotelier = hotelier;
        return this;
    }

    public void setHotelier(String hotelier) {
        this.hotelier = hotelier;
    }

    public String getCategory() {
        return category;
    }

    public Accommodation category(String category) {
        this.category = category;
        return this;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Location getLocation() {
        return location;
    }

    public Accommodation location(Location location) {
        this.location = location;
        return this;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Accommodation)) {
            return false;
        }
        return id != null && id.equals(((Accommodation) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Accommodation{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", hotelier='" + getHotelier() + "'" +
            ", category='" + getCategory() + "'" +
            "}";
    }
}
