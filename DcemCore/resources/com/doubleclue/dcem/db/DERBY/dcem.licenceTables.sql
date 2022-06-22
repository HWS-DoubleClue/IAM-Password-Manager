create table licence_cluster (
dc_id integer not null,
clusterId varchar(255) not null,
clusterName varchar(255) not null,
createdOn timestamp not null,
dc_disabled boolean not null,
information varchar(255),
productive boolean,
dc_customer integer not null,
primary key (dc_id)
);

create table licence_customer (
dc_id integer not null,
adress varchar(255) not null,
contact_email varchar(255),
contact_name varchar(255),
country varchar(128) not null,
name varchar(255) not null,
zip_code varchar(255),
primary key (dc_id)
);

create table licence_orders (
dc_id integer not null,
lastModified timestamp not null,
limitationMap varchar(4096) not null,
moduleID varchar(255),
tenantID varchar(255),
clusterId integer not null,
customerId integer not null,
lastModifiedBy integer not null,
primary key (dc_id)
);

create table licence_otp_token (
dc_id integer not null,
otpType integer not null,
secretKey long varchar for bit data not null,
serialNumber varchar(255) not null,
dc_customer integer,
primary key (dc_id)
);
create unique index UK_LICENCE_CLUSTER_ID on licence_cluster (clusterId);
create unique index UK_LICENCE_CLUSTER_NAME on licence_cluster (clusterName);
create unique index UK_CUSTOMER_NAME on licence_customer (name);
create unique index UK_LICENCE_OTP_SERIAL on licence_otp_token (serialNumber);

alter table licence_cluster
add constraint FK_CLUSTER_CUSTOMER
foreign key (dc_customer)
references licence_customer;

alter table licence_orders
add constraint FK_licence_orders_license_cluster_id
foreign key (clusterId)
references licence_cluster;

alter table licence_orders
add constraint FK_licence_orders_license_customer_id
foreign key (customerId)
references licence_customer;

alter table licence_orders
add constraint FK_license_orders_core_user_id
foreign key (lastModifiedBy)
references core_user;

alter table licence_otp_token
add constraint FK_OTP_CUSTOMER
foreign key (dc_customer)
references licence_customer;
