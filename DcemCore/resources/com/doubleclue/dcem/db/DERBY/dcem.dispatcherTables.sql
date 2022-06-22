create table dispatcher_company (
dc_id integer not null,
adress varchar(255) not null,
companyName varchar(255) not null,
contact_email varchar(255),
contact_name varchar(255),
country varchar(128) not null,
remarks varchar(255),
zip_code varchar(255),
primary key (dc_id)
);

create table dispatcher_registration (
dc_id integer not null,
clusterId varchar(255),
dcemName varchar(255) not null,
dc_disabled boolean not null,
isReverseProxy boolean not null,
lastConnection timestamp,
lastModified timestamp not null,
maxReverseProxySessions integer not null,
remarks varchar(255),
sdkConfigDcemContent blob,
company_dc_id integer not null,
lastModifiedBy integer not null,
primary key (dc_id)
);
create unique index UK_DISPATCHER_COMPANY_NAME on dispatcher_company (companyName);
create unique index UK_REGISTRATION_NAME on dispatcher_registration (dcemName);

alter table dispatcher_registration
add constraint FK_dispacher_reg_company
foreign key (company_dc_id)
references dispatcher_company;

alter table dispatcher_registration
add constraint FK_dispacher_reg_operator
foreign key (lastModifiedBy)
references core_user;
