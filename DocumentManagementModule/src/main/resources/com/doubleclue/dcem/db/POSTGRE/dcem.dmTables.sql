
create table dm_workflow (
dc_id  serial not null,
day int4 not null,
description varchar(255),
localDate date,
month int4 not null,
dc_name varchar(128),
workflowAction int4,
workflowTrigger int4,
cloudSafeEntity int4 not null,
user_dc_id int4 not null,
primary key (dc_id)
);

alter table if exists dm_workflow
add constraint UK_WORKLFOW_NAME unique (dc_name, cloudSafeEntity);

alter table if exists dm_workflow
add constraint FK_DM_WORKFLOW_CLOUDSAFE
foreign key (cloudSafeEntity)
references as_cloudsafe;

alter table if exists dm_workflow
add constraint FK_DM_WORKFLOW_USER
foreign key (user_dc_id)
references core_user;
