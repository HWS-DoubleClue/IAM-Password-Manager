create table dispatcher_company (
dc_id number(10,0) not null,
adress varchar2(255 char) not null,
companyName varchar2(255 char) not null,
contact_email varchar2(255 char),
contact_name varchar2(255 char),
country varchar2(128 char) not null,
remarks varchar2(255 char),
zip_code varchar2(255 char),
primary key (dc_id)
);

create table dispatcher_registration (
dc_id number(10,0) not null,
clusterId varchar2(255 char),
dcemName varchar2(255 char) not null,
dc_disabled number(1,0) not null,
isReverseProxy number(1,0) not null,
lastConnection timestamp,
lastModified timestamp not null,
maxReverseProxySessions number(10,0) not null,
remarks varchar2(255 char),
sdkConfigDcemContent blob,
company_dc_id number(10,0) not null,
lastModifiedBy number(10,0) not null,
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
