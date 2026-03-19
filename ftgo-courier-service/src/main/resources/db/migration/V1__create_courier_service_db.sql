create table courier
(
  id                       bigint not null auto_increment,
  available bit,
  first_name varchar(255),
  last_name varchar(255),
  street1 varchar(255),
  street2 varchar(255),
  city    varchar(255),
  state   varchar(255),
  zip     varchar(255),
  primary key (id)
) engine = InnoDB;
