create table pd_agent (
dc_id int not null,
dc_enabled bit not null,
serviceAgentId varchar(255) not null,
serviceAgentName varchar(255) not null,
dc_user int not null,
pd_services int not null,
primary key (dc_id)
);

create table pd_agent_seq (
next_val bigint
);

insert into pd_agent_seq values ( 1 );

create table pd_cache (
dc_id bigint not null,
endDate datetime2 not null,
period int,
queryType int not null,
startDate datetime2 not null,
pd_group int,
primary key (dc_id)
);

create table pd_cache_value (
dc_id bigint not null,
additionalData varchar(MAX),
cacheValue int not null,
result int not null,
pd_agent int,
pd_record bigint not null,
primary key (dc_id)
);

create table pd_group (
dc_id int not null,
dc_enabled bit not null,
serviceGroupId varchar(255) not null,
serviceGroupName varchar(255) not null,
pd_services int not null,
primary key (dc_id)
);

create table pd_group_seq (
next_val bigint
);

insert into pd_group_seq values ( 1 );

create table pd_query (
dc_id int not null,
queryAgent varchar(4095),
queryGroup varchar(4095),
queryType int not null,
pd_services int not null,
primary key (dc_id)
);

create table pd_query_seq (
next_val bigint
);

insert into pd_query_seq values ( 1 );

create table pd_ref_agent_group (
group_id int not null,
agent_id int not null
);

create table pd_service (
dc_id int not null,
backgroundTaskTimeMinutes int not null,
dc_enabled bit not null,
serviceName varchar(255) not null,
servicePassword varbinary(MAX) not null,
serviceType int not null,
serviceUsername varchar(255) not null,
serviceUrl varchar(255) not null,
primary key (dc_id)
);

create table pd_service_seq (
next_val bigint
);

insert into pd_service_seq values ( 1 );

alter table pd_agent
add constraint UK_PD_AGENT unique (pd_services, serviceAgentId);

alter table pd_group
add constraint UK_PD_SERVICE_GROUP_ID unique (serviceGroupId, pd_services);

alter table pd_query
add constraint UK_PD_QUERIES_SERVICE unique (pd_services, queryType);

alter table pd_ref_agent_group
add constraint UK_GROUP_MEMBERS unique (group_id, agent_id);

alter table pd_service
add constraint UK_PD_SERVICE unique (serviceName);

alter table pd_agent
add constraint FK_AGENT_USER
foreign key (dc_user)
references core_user
on delete cascade;

alter table pd_agent
add constraint FK_AGENT_SERVICES
foreign key (pd_services)
references pd_service
on delete cascade;

alter table pd_cache
add constraint FK_RECORD_GROUP
foreign key (pd_group)
references pd_group
on delete cascade;

alter table pd_cache_value
add constraint FK_CACHE_VALUE_AGENT
foreign key (pd_agent)
references pd_agent;

alter table pd_cache_value
add constraint FK_CACHE_VALUE_RECORD
foreign key (pd_record)
references pd_cache;

alter table pd_group
add constraint FK_GROUP_SERVICES
foreign key (pd_services)
references pd_service
on delete cascade;

alter table pd_query
add constraint FK_QUERY_SERVICES
foreign key (pd_services)
references pd_service
on delete cascade;

alter table pd_ref_agent_group
add constraint FK_GROUP_AGENT
foreign key (agent_id)
references pd_agent;

alter table pd_ref_agent_group
add constraint FK_AGENT_GROUP
foreign key (group_id)
references pd_group
on delete cascade;
