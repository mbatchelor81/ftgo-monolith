package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.Address;
import net.chrisrichardson.ftgo.common.PersonName;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;

@Entity
@Access(AccessType.FIELD)
@DynamicUpdate
public class Courier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  private PersonName name;

  @Embedded
  private Address address;

  private Boolean available;

  public Courier() {
  }

  public Courier(PersonName name, Address address) {
    this.name = name;
    this.address = address;
  }

  public void noteAvailable() {
    this.available = true;
  }

  public boolean isAvailable() {
    return available;
  }

  public Long getId() {
    return id;
  }

  public void noteUnavailable() {
    this.available = false;
  }

  public PersonName getName() {
    return name;
  }

  public Address getAddress() {
    return address;
  }
}
