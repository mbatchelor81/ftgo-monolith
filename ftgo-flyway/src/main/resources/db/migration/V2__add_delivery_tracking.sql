use ftgo;

-- Add latitude/longitude columns to courier table
alter table courier add column latitude double;
alter table courier add column longitude double;

-- Add latitude/longitude columns to restaurants table
alter table restaurants add column latitude double;
alter table restaurants add column longitude double;

-- Create delivery_tracking table
create table delivery_tracking
(
  id                  bigint not null auto_increment,
  order_id            bigint not null,
  courier_id          bigint not null,
  status              varchar(50) not null,
  estimated_pickup_time   datetime,
  estimated_delivery_time datetime,
  distance_km         double,
  created_at          datetime not null,
  updated_at          datetime not null,
  primary key (id)
) engine = InnoDB;

alter table delivery_tracking
  add constraint delivery_tracking_order_id foreign key (order_id) references orders (id);

alter table delivery_tracking
  add constraint delivery_tracking_courier_id foreign key (courier_id) references courier (id);

create index idx_delivery_tracking_order_id on delivery_tracking (order_id);
create index idx_delivery_tracking_courier_id on delivery_tracking (courier_id);
create index idx_delivery_tracking_status on delivery_tracking (status);
