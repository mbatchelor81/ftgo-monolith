package com.ftgo.common.jpa;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

/**
 * Base class for JPA entities providing common id and version fields.
 *
 * <p>Subclasses inherit a database-generated identity column and an optimistic
 * locking version column.  This avoids repeating boilerplate across every
 * entity in the FTGO domain.</p>
 */
@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }
}
