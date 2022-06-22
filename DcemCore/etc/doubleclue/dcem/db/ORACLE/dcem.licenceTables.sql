create table licence_cluster (
dc_id number(10,0) not null,
clusterId varchar2(255 char) not null,
clusterName varchar2(255 char) not null,
createdOn timestamp not null,
dc_disabled number(1,0) not null,
information varchar2(255 char),
productive number(1,0),
dc_customer number(10,0) not null,
primary key (dc_id)
);

create table licence_customer (
dc_id number(10,0) not null,
adress varchar2(255 char) not null,
contact_email varchar2(255 char),
contact_name varchar2(255 char),
country varchar2(128 char) not null,
name varchar2(255 char) not null,
zip_code varchar2(255 char),
primary key (dc_id)
);

create table licence_orders (
dc_id number(10,0) not null,
lastModified timestamp not null,
limitationMap long not null,
moduleID varchar2(255 char),
tenantID varchar2(255 char),
clusterId number(10,0) not null,
customerId number(10,0) not null,
lastModifiedBy number(10,0) not null,
primary key (dc_id)
);

create table licence_otp_token (
dc_id number(10,0) not null,
otpType number(10,0) not null,
secretKey long raw not null,
serialNumber varchar2(255 char) not null,
dc_customer number(10,0),
primary key (dc_id)
);

alter table licence_cluster
add constraint UK_LICENCE_CLUSTER_ID unique (clusterId);

alter table licence_cluster
add constraint UK_LICENCE_CLUSTER_NAME unique (clusterName);

alter table licence_customer
add constraint UK_CUSTOMER_NAME unique (name);

alter table licence_otp_token
add constraint UK_LICENCE_OTP_SERIAL unique (serialNumber);

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
