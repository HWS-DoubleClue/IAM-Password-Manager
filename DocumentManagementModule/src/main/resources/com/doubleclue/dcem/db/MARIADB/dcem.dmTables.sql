
create table dm_workflow (
dc_id integer not null auto_increment,
day integer not null,
description varchar(255),
localDate date,
month integer not null,
dc_name varchar(128),
workflowAction integer,
workflowTrigger integer,
cloudSafeEntity integer not null,
user_dc_id integer not null,
primary key (dc_id)
) engine=InnoDB;

alter table dm_workflow
add constraint UK_WORKLFOW_NAME unique (dc_name, cloudSafeEntity);

alter table dm_workflow
add constraint FK_DM_WORKFLOW_CLOUDSAFE
foreign key (cloudSafeEntity)
references as_cloudsafe (dc_id);

alter table dm_workflow
add constraint FK_DM_WORKFLOW_USER
foreign key (user_dc_id)
references core_user (dc_id);
