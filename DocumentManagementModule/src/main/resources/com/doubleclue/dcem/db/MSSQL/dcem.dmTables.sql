
create table dm_workflow (
dc_id int identity not null,
day int not null,
description varchar(255),
localDate date,
month int not null,
dc_name varchar(128),
workflowAction int,
workflowTrigger int,
cloudSafeEntity int not null,
user_dc_id int not null,
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
