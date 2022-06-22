create sequence pd_agent_seq start 1 increment 4;
create sequence pd_group_seq start 1 increment 4;
create sequence pd_query_seq start 1 increment 4;
create sequence pd_service_seq start 1 increment 4;

    create table core_action (
       dc_id int4 not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
        primary key (dc_id)
    );

    create table core_group (
       dc_id int4 not null,
        jpaVersion int4 not null,
        description varchar(255),
        groupDn varchar(255),
        dc_name varchar(255) not null,
        dc_role int4,
        dc_ldap int4,
        primary key (dc_id)
    );

    create table core_ldap (
       dc_id int4 not null,
        baseDN varchar(255) not null,
        configJson varchar(4096),
        domainType int4 not null,
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
        password bytea not null,
        dc_rank int4,
        searchAccount varchar(255) not null,
        telephoneAttribute varchar(255),
        dc_version int4,
        primary key (dc_id)
    );

    create table core_ref_user_group (
       group_id int4 not null,
        user_id int4 not null
    );

    create table core_role (
       dc_id int4 not null,
        disabled boolean not null,
        jpaVersion int4 not null,
        dc_name varchar(64) not null,
        dc_rank int4 not null,
        systemRole boolean not null,
        primary key (dc_id)
    );

    create table core_role_core_action (
       core_role_dc_id int4 not null,
        actions_dc_id int4 not null,
        primary key (core_role_dc_id, actions_dc_id)
    );

    create table core_seq (
       seq_name varchar(255) not null,
        seq_value int8,
        primary key (seq_name)
    );

    insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id int4 not null,
        acSuspendedTill timestamp,
        disabled boolean not null,
        displayName varchar(128),
        email varchar(255),
        failActivations int4 not null,
        hashPassword bytea,
        hmac bytea not null,
        jpaVersion int4 not null,
        locale int4,
        lastLogin timestamp,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        objectGuid bytea,
        passCounter int4 not null,
        prvMobile varchar(32),
        dc_salt bytea,
        saveit bytea,
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(128),
        dc_role int4 not null,
        dc_ldap int4,
        primary key (dc_id)
    );

    create table pd_agent (
       dc_id int4 not null,
        dc_enabled boolean not null,
        serviceAgentId varchar(255) not null,
        serviceAgentName varchar(255) not null,
        dc_user int4 not null,
        pd_services int4 not null,
        primary key (dc_id)
    );

    create table pd_cache (
       dc_id int8 not null,
        endDate timestamp not null,
        period int4,
        queryType int4 not null,
        startDate timestamp not null,
        pd_group int4,
        primary key (dc_id)
    );

    create table pd_cache_value (
       dc_id int8 not null,
        additionalData varchar(8192),
        cacheValue int4 not null,
        result int4 not null,
        pd_agent int4,
        pd_record int8 not null,
        primary key (dc_id)
    );

    create table pd_group (
       dc_id int4 not null,
        dc_enabled boolean not null,
        serviceGroupId varchar(255) not null,
        serviceGroupName varchar(255) not null,
        pd_services int4 not null,
        primary key (dc_id)
    );

    create table pd_query (
       dc_id int4 not null,
        queryAgent varchar(4095),
        queryGroup varchar(4095),
        queryType int4 not null,
        pd_services int4 not null,
        primary key (dc_id)
    );

    create table pd_ref_agent_group (
       group_id int4 not null,
        agent_id int4 not null
    );

    create table pd_service (
       dc_id int4 not null,
        backgroundTaskTimeMinutes int4 not null,
        dc_enabled boolean not null,
        serviceName varchar(255) not null,
        servicePassword bytea not null,
        serviceType int4 not null,
        serviceUsername varchar(255) not null,
        serviceUrl varchar(255) not null,
        primary key (dc_id)
    );

    alter table if exists core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table if exists core_group 
       add constraint UK_APP_GROUP unique (dc_name);

    alter table if exists core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table if exists core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table if exists core_user 
       add constraint UK_APP_USER unique (loginId);

    alter table if exists pd_agent 
       add constraint UK_PD_AGENT unique (pd_services, serviceAgentId);

    alter table if exists pd_group 
       add constraint UK_PD_SERVICE_GROUP_ID unique (serviceGroupId, pd_services);

    alter table if exists pd_query 
       add constraint UK_PD_QUERIES_SERVICE unique (pd_services, queryType);

    alter table if exists pd_ref_agent_group 
       add constraint UK_GROUP_MEMBERS unique (group_id, agent_id);

    alter table if exists pd_service 
       add constraint UK_PD_SERVICE unique (serviceName);

    alter table if exists core_group 
       add constraint FK_GROUP_ROLE 
       foreign key (dc_role) 
       references core_role;

    alter table if exists core_group 
       add constraint FK_GROUP_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table if exists core_ref_user_group 
       add constraint FK_GROUP_USER 
       foreign key (user_id) 
       references core_user;

    alter table if exists core_ref_user_group 
       add constraint FK_USER_GROUP 
       foreign key (group_id) 
       references core_group;

    alter table if exists core_role_core_action 
       add constraint FK_ROLE_ACTION 
       foreign key (actions_dc_id) 
       references core_action;

    alter table if exists core_role_core_action 
       add constraint FKm8fcladhxpesfv9gs7r0leqqg 
       foreign key (core_role_dc_id) 
       references core_role;

    alter table if exists core_user 
       add constraint FK_USER_ROLE 
       foreign key (dc_role) 
       references core_role;

    alter table if exists core_user 
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table if exists pd_agent 
       add constraint FK_AGENT_USER 
       foreign key (dc_user) 
       references core_user 
       on delete cascade;

    alter table if exists pd_agent 
       add constraint FK_AGENT_SERVICES 
       foreign key (pd_services) 
       references pd_service 
       on delete cascade;

    alter table if exists pd_cache 
       add constraint FK_RECORD_GROUP 
       foreign key (pd_group) 
       references pd_group 
       on delete cascade;

    alter table if exists pd_cache_value 
       add constraint FK_CACHE_VALUE_AGENT 
       foreign key (pd_agent) 
       references pd_agent;

    alter table if exists pd_cache_value 
       add constraint FK_CACHE_VALUE_RECORD 
       foreign key (pd_record) 
       references pd_cache;

    alter table if exists pd_group 
       add constraint FK_GROUP_SERVICES 
       foreign key (pd_services) 
       references pd_service 
       on delete cascade;

    alter table if exists pd_query 
       add constraint FK_QUERY_SERVICES 
       foreign key (pd_services) 
       references pd_service 
       on delete cascade;

    alter table if exists pd_ref_agent_group 
       add constraint FK_GROUP_AGENT 
       foreign key (agent_id) 
       references pd_agent;

    alter table if exists pd_ref_agent_group 
       add constraint FK_AGENT_GROUP 
       foreign key (group_id) 
       references pd_group 
       on delete cascade;
