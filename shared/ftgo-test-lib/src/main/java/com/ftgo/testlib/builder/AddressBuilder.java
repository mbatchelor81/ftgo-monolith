package com.ftgo.testlib.builder;

import net.chrisrichardson.ftgo.common.Address;

/**
 * Builder for {@link Address} test instances.
 *
 * <pre>{@code
 * Address addr = AddressBuilder.address().withCity("Oakland").build();
 * }</pre>
 */
public final class AddressBuilder {

    private String street1 = "123 Main St";
    private String street2 = "Suite 100";
    private String city = "Oakland";
    private String state = "CA";
    private String zip = "94611";

    private AddressBuilder() {
    }

    public static AddressBuilder address() {
        return new AddressBuilder();
    }

    public AddressBuilder withStreet1(String street1) {
        this.street1 = street1;
        return this;
    }

    public AddressBuilder withStreet2(String street2) {
        this.street2 = street2;
        return this;
    }

    public AddressBuilder withCity(String city) {
        this.city = city;
        return this;
    }

    public AddressBuilder withState(String state) {
        this.state = state;
        return this;
    }

    public AddressBuilder withZip(String zip) {
        this.zip = zip;
        return this;
    }

    public Address build() {
        return new Address(street1, street2, city, state, zip);
    }
}
