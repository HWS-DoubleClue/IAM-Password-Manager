create table dispatcher_company (
dc_id int not null,
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
dc_id int not null,
clusterId varchar(255),
dcemName varchar(255) not null,
dc_disabled bit not null,
isReverseProxy bit not null,
lastConnection datetime2,
lastModified datetime2 not null,
maxReverseProxySessions int not null,
remarks varchar(255),
sdkConfigDcemContent varbinary(MAX),
company_dc_id int not null,
lastModifiedBy int not null,
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
