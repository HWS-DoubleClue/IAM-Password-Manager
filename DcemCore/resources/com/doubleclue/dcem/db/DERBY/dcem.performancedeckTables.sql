create table pd_agent (
dc_id integer not null,
dc_enabled boolean not null,
serviceAgentId varchar(255) not null,
serviceAgentName varchar(255) not null,
dc_user integer not null,
pd_services integer not null,
primary key (dc_id)
);

create table pd_cache (
dc_id bigint not null,
endDate timestamp not null,
period integer,
queryType integer not null,
startDate timestamp not null,
pd_group integer,
primary key (dc_id)
);

create table pd_cache_value (
dc_id bigint not null,
additionalData varchar(8192),
cacheValue integer not null,
result integer not null,
pd_agent integer,
pd_record bigint not null,
primary key (dc_id)
);

create table pd_group (
dc_id integer not null,
dc_enabled boolean not null,
serviceGroupId varchar(255) not null,
serviceGroupName varchar(255) not null,
pd_services integer not null,
primary key (dc_id)
);

create table pd_query (
dc_id integer not null,
queryAgent varchar(4095),
queryGroup varchar(4095),
queryType integer not null,
pd_services integer not null,
primary key (dc_id)
);

create table pd_ref_agent_group (
group_id integer not null,
agent_id integer not null
);

create table pd_service (
dc_id integer not null,
backgroundTaskTimeMinutes integer not null,
dc_enabled boolean not null,
serviceName varchar(255) not null,
servicePassword long varchar for bit data not null,
serviceType integer not null,
serviceUsername varchar(255) not null,
serviceUrl varchar(255) not null,
primary key (dc_id)
);
create unique index UK_PD_AGENT on pd_agent (pd_services, serviceAgentId);
create unique index UK_PD_SERVICE_GROUP_ID on pd_group (serviceGroupId, pd_services);
create unique index UK_PD_QUERIES_SERVICE on pd_query (pd_services, queryType);
create unique index UK_GROUP_MEMBERS on pd_ref_agent_group (group_id, agent_id);
create unique index UK_PD_SERVICE on pd_service (serviceName);

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
