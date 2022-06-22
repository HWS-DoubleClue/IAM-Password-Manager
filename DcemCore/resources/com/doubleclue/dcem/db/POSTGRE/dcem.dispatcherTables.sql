create table dispatcher_company (
dc_id int4 not null,
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
dc_id int4 not null,
clusterId varchar(255),
dcemName varchar(255) not null,
dc_disabled boolean not null,
isReverseProxy boolean not null,
lastConnection timestamp,
lastModified timestamp not null,
maxReverseProxySessions int4 not null,
remarks varchar(255),
sdkConfigDcemContent oid,
company_dc_id int4 not null,
lastModifiedBy int4 not null,
primary key (dc_id)
);

alter table dispatcher_company
add constraint UK_DISPATCHER_COMPANY_NAME unique (companyName);

alter table dispatcher_registration
add constraint UK_REGISTRATION_NAME unique (dcemName);

alter table dispatcher_registration
add constraint FK_dispacher_reg_company
foreign key (company_dc_id)
references dispatcher_company;

alter table dispatcher_registration
add constraint FK_dispacher_reg_operator
foreign key (lastModifiedBy)
references core_user;
