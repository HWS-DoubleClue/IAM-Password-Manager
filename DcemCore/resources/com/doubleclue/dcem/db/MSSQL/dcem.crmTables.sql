create table crm_art (
dc_id int identity not null,
dc_name varchar(255),
primary key (dc_id)
);

create table crm_bundesland (
dc_id int identity not null,
dc_name varchar(128) not null,
primary key (dc_id)
);

create table crm_city (
dc_id int identity not null,
dc_name varchar(128) not null,
primary key (dc_id)
);

create table crm_customer (
dc_id int identity not null,
dc_addedAt date,
dc_bemerkung varchar(255),
dc_disabled bit,
dc_geoDataAdress varchar(255),
dc_mail varchar(128) not null,
dc_name varchar(128) not null,
dc_postCode varchar(255),
dc_quelle varchar(255),
dc_street varchar(255),
dc_artID int,
dc_bundeslandID int,
dc_cityID int,
dc_landID int,
primary key (dc_id)
);

create table crm_land (
dc_id int identity not null,
dc_name varchar(128) not null,
primary key (dc_id)
);

alter table crm_city
add constraint UK_CRM_CITY_NAME unique (dc_name);

alter table crm_customer
add constraint UK_CRM_NAME unique (dc_name);

alter table crm_customer
add constraint FK_CUSTOMER_ART
foreign key (dc_artID)
references crm_art;

alter table crm_customer
add constraint FK_CUSTOMER_BUNDESLAND
foreign key (dc_bundeslandID)
references crm_bundesland;

alter table crm_customer
add constraint FK_CUSTOMER_CITY
foreign key (dc_cityID)
references crm_city;

alter table crm_customer
add constraint FK_CUSTOMER_LAND
foreign key (dc_landID)
references crm_land;
