create table deliveries
(
  id          bigint not null auto_increment,
  order_id    bigint,
  courier_id  bigint,
  pickup_time datetime,
  dropoff_time datetime,
  status      varchar(255),
  primary key (id)
) engine = InnoDB;

create table delivery_actions
(
  delivery_id bigint not null,
  order_id    bigint,
  time        datetime,
  type        varchar(255)
) engine = InnoDB;

alter table delivery_actions
  add constraint delivery_actions_delivery_id foreign key (delivery_id) references deliveries (id);
