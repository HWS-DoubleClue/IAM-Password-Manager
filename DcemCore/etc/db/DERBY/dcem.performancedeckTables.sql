create sequence pd_agent_seq start with 1 increment by 4;
create sequence pd_group_seq start with 1 increment by 4;
create sequence pd_query_seq start with 1 increment by 4;
create sequence pd_service_seq start with 1 increment by 4;

    create table core_action (
       dc_id integer not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
        primary key (dc_id)
    );

    create table core_group (
       dc_id integer not null,
        jpaVersion integer not null,
        description varchar(255),
        groupDn varchar(255),
        dc_name varchar(255) not null,
        dc_role integer,
        dc_ldap integer,
        primary key (dc_id)
    );

    create table core_ldap (
       dc_id integer not null,
        baseDN varchar(255) not null,
        configJson varchar(4096),
        domainType integer not null,
        enable boolean not null,
        filter varchar(255) not null,
        firstNameAttribute varchar(255) not null,
        host varchar(255) not null,
        lastNameAttribute varchar(255) not null,
        loginAttribute varchar(255) not null,
        mailAttribute varchar(255),
        mapEmailDomains varchar(255),
        mobileAttribute varchar(255),
        name varchar(64) not null,
        password long varchar for bit data not null,
        dc_rank integer,
        searchAccount varchar(255) not null,
        telephoneAttribute varchar(255),
        dc_version integer,
        primary key (dc_id)
    );

    create table core_ref_user_group (
       group_id integer not null,
        user_id integer not null
    );

    create table core_role (
       dc_id integer not null,
        disabled boolean not null,
        jpaVersion integer not null,
        dc_name varchar(64) not null,
        dc_rank integer not null,
        systemRole boolean not null,
        primary key (dc_id)
    );

    create table core_role_core_action (
       core_role_dc_id integer not null,
        actions_dc_id integer not null,
        primary key (core_role_dc_id, actions_dc_id)
    );

    create table core_seq (
       seq_name varchar(255) not null,
        seq_value bigint,
        primary key (seq_name)
    );

    insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id integer not null,
        acSuspendedTill timestamp,
        disabled boolean not null,
        displayName varchar(128),
        email varchar(255),
        failActivations integer not null,
        hashPassword long varchar for bit data,
        hmac varchar(32) for bit data not null,
        jpaVersion integer not null,
        locale integer,
        lastLogin timestamp,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        objectGuid varchar(255) for bit data,
        passCounter integer not null,
        prvMobile varchar(32),
        dc_salt varchar(32) for bit data,
        saveit long varchar for bit data,
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(128),
        dc_role integer not null,
        dc_ldap integer,
        primary key (dc_id)
    );

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
create unique index UK_SEM_ACTION on core_action (moduleId, subject, action);
create unique index UK_APP_GROUP on core_group (dc_name);
create unique index UK_LDAP_NAME on core_ldap (name);
create unique index UK_ROLE_NAME on core_role (dc_name);
create unique index UK_APP_USER on core_user (loginId);
create unique index UK_PD_AGENT on pd_agent (pd_services, serviceAgentId);
create unique index UK_PD_SERVICE_GROUP_ID on pd_group (serviceGroupId, pd_services);
create unique index UK_PD_QUERIES_SERVICE on pd_query (pd_services, queryType);
create unique index UK_GROUP_MEMBERS on pd_ref_agent_group (group_id, agent_id);
create unique index UK_PD_SERVICE on pd_service (serviceName);

    alter table core_group 
       add constraint FK_GROUP_ROLE 
       foreign key (dc_role) 
       references core_role;

    alter table core_group 
       add constraint FK_GROUP_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table core_ref_user_group 
       add constraint FK_GROUP_USER 
       foreign key (user_id) 
       references core_user;

    alter table core_ref_user_group 
       add constraint FK_USER_GROUP 
       foreign key (group_id) 
       references core_group;

    alter table core_role_core_action 
       add constraint FK_ROLE_ACTION 
       foreign key (actions_dc_id) 
       references core_action;

    alter table core_role_core_action 
       add constraint FKm8fcladhxpesfv9gs7r0leqqg 
       foreign key (core_role_dc_id) 
       references core_role;

    alter table core_user 
       add constraint FK_USER_ROLE 
       foreign key (dc_role) 
       references core_role;

    alter table core_user 
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

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
