create table pd_agent (
dc_id number(10,0) not null,
dc_enabled number(1,0) not null,
serviceAgentId varchar2(255 char) not null,
serviceAgentName varchar2(255 char) not null,
dc_user number(10,0) not null,
pd_services number(10,0) not null,
primary key (dc_id)
);

create table pd_cache (
dc_id number(19,0) not null,
endDate timestamp not null,
period number(10,0),
queryType number(10,0) not null,
startDate timestamp not null,
pd_group number(10,0),
primary key (dc_id)
);

create table pd_cache_value (
dc_id number(19,0) not null,
additionalData long,
cacheValue number(10,0) not null,
result number(10,0) not null,
pd_agent number(10,0),
pd_record number(19,0) not null,
primary key (dc_id)
);

create table pd_group (
dc_id number(10,0) not null,
dc_enabled number(1,0) not null,
serviceGroupId varchar2(255 char) not null,
serviceGroupName varchar2(255 char) not null,
pd_services number(10,0) not null,
primary key (dc_id)
);

create table pd_query (
dc_id number(10,0) not null,
queryAgent long,
queryGroup long,
queryType number(10,0) not null,
pd_services number(10,0) not null,
primary key (dc_id)
);

create table pd_ref_agent_group (
group_id number(10,0) not null,
agent_id number(10,0) not null
);

create table pd_service (
dc_id number(10,0) not null,
backgroundTaskTimeMinutes number(10,0) not null,
dc_enabled number(1,0) not null,
serviceName varchar2(255 char) not null,
servicePassword long raw not null,
serviceType number(10,0) not null,
serviceUsername varchar2(255 char) not null,
serviceUrl varchar2(255 char) not null,
primary key (dc_id)
);

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
