
create table dm_workflow (
dc_id number(10,0) generated as identity,
day number(10,0) not null,
description varchar2(255 char),
localDate date,
month number(10,0) not null,
dc_name varchar2(128 char),
workflowAction number(10,0),
workflowTrigger number(10,0),
cloudSafeEntity number(10,0) not null,
user_dc_id number(10,0) not null,
primary key (dc_id)
);

alter table dm_workflow
add constraint UK_WORKLFOW_NAME unique (dc_name, cloudSafeEntity);

alter table dm_workflow
add constraint FK_DM_WORKFLOW_CLOUDSAFE
foreign key (cloudSafeEntity)
references as_cloudsafe;

alter table dm_workflow
add constraint FK_DM_WORKFLOW_USER
foreign key (user_dc_id)
references core_user;
