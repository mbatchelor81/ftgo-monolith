package com.ftgo.common;

import jakarta.persistence.Embeddable;

/**
 * Embeddable value object representing a person's first and last name.
 */
@Embeddable
public class PersonName {

    private String firstName;
    private String lastName;

    private PersonName() {
    }

    /**
     * Creates a new PersonName with the given first and last name.
     *
     * @param firstName the first name
     * @param lastName  the last name
     */
    public PersonName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
