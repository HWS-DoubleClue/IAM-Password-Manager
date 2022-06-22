
    create table core_action (
       dc_id integer not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table core_group (
       dc_id integer not null,
        jpaVersion integer not null,
        description varchar(255),
        groupDn varchar(255),
        dc_name varchar(255) not null,
        dc_role integer,
        dc_ldap integer,
        primary key (dc_id)
    ) engine=InnoDB;

    create table core_ldap (
       dc_id integer not null,
        baseDN varchar(255) not null,
        configJson varchar(4096),
        domainType integer not null,
        enable bit not null,
        filter varchar(255) not null,
        firstNameAttribute varchar(255) not null,
        host varchar(255) not null,
        lastNameAttribute varchar(255) not null,
        loginAttribute varchar(255) not null,
        mailAttribute varchar(255),
        mapEmailDomains varchar(255),
        mobileAttribute varchar(255),
        name varchar(64) not null,
        password mediumblob not null,
        dc_rank integer,
        searchAccount varchar(255) not null,
        telephoneAttribute varchar(255),
        dc_version integer,
        primary key (dc_id)
    ) engine=InnoDB;

    create table core_ref_user_group (
       group_id integer not null,
        user_id integer not null
    ) engine=InnoDB;

    create table core_role (
       dc_id integer not null,
        disabled bit not null,
        jpaVersion integer not null,
        dc_name varchar(64) not null,
        dc_rank integer not null,
        systemRole bit not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table core_role_core_action (
       core_role_dc_id integer not null,
        actions_dc_id integer not null,
        primary key (core_role_dc_id, actions_dc_id)
    ) engine=InnoDB;

    create table core_seq (
       seq_name varchar(255) not null,
        seq_value bigint,
        primary key (seq_name)
    ) engine=InnoDB;

    insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id integer not null,
        acSuspendedTill datetime,
        disabled bit not null,
        displayName varchar(128),
        email varchar(255),
        failActivations integer not null,
        hashPassword mediumblob,
        hmac tinyblob not null,
        jpaVersion integer not null,
        locale integer,
        lastLogin datetime,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        objectGuid tinyblob,
        passCounter integer not null,
        prvMobile varchar(32),
        dc_salt tinyblob,
        saveit mediumblob,
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(128),
        dc_role integer not null,
        dc_ldap integer,
        primary key (dc_id)
    ) engine=InnoDB;

    create table pd_agent (
       dc_id integer not null,
        dc_enabled bit not null,
        serviceAgentId varchar(255) not null,
        serviceAgentName varchar(255) not null,
        dc_user integer not null,
        pd_services integer not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table pd_agent_seq (
       next_val bigint
    ) engine=InnoDB;

    insert into pd_agent_seq values ( 1 );

    create table pd_cache (
       dc_id bigint not null,
        endDate datetime not null,
        period integer,
        queryType integer not null,
        startDate datetime not null,
        pd_group integer,
        primary key (dc_id)
    ) engine=InnoDB;

    create table pd_cache_value (
       dc_id bigint not null,
        additionalData varchar(8192),
        cacheValue integer not null,
        result integer not null,
        pd_agent integer,
        pd_record bigint not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table pd_group (
       dc_id integer not null,
        dc_enabled bit not null,
        serviceGroupId varchar(255) not null,
        serviceGroupName varchar(255) not null,
        pd_services integer not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table pd_group_seq (
       next_val bigint
    ) engine=InnoDB;

    insert into pd_group_seq values ( 1 );

    create table pd_query (
       dc_id integer not null,
        queryAgent varchar(4095),
        queryGroup varchar(4095),
        queryType integer not null,
        pd_services integer not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table pd_query_seq (
       next_val bigint
    ) engine=InnoDB;

    insert into pd_query_seq values ( 1 );

    create table pd_ref_agent_group (
       group_id integer not null,
        agent_id integer not null
    ) engine=InnoDB;

    create table pd_service (
       dc_id integer not null,
        backgroundTaskTimeMinutes integer not null,
        dc_enabled bit not null,
        serviceName varchar(255) not null,
        servicePassword mediumblob not null,
        serviceType integer not null,
        serviceUsername varchar(255) not null,
        serviceUrl varchar(255) not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table pd_service_seq (
       next_val bigint
    ) engine=InnoDB;

    insert into pd_service_seq values ( 1 );

    alter table core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table core_group 
       add constraint UK_APP_GROUP unique (dc_name);

    alter table core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table core_user 
       add constraint UK_APP_USER unique (loginId);

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

    alter table core_group 
       add constraint FK_GROUP_ROLE 
       foreign key (dc_role) 
       references core_role (dc_id);

    alter table core_group 
       add constraint FK_GROUP_LDAP 
       foreign key (dc_ldap) 
       references core_ldap (dc_id);

    alter table core_ref_user_group 
       add constraint FK_GROUP_USER 
       foreign key (user_id) 
       references core_user (dc_id);

    alter table core_ref_user_group 
       add constraint FK_USER_GROUP 
       foreign key (group_id) 
       references core_group (dc_id);

    alter table core_role_core_action 
       add constraint FK_ROLE_ACTION 
       foreign key (actions_dc_id) 
       references core_action (dc_id);

    alter table core_role_core_action 
       add constraint FKm8fcladhxpesfv9gs7r0leqqg 
       foreign key (core_role_dc_id) 
       references core_role (dc_id);

    alter table core_user 
       add constraint FK_USER_ROLE 
       foreign key (dc_role) 
       references core_role (dc_id);

    alter table core_user 
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap (dc_id);

    alter table pd_agent 
       add constraint FK_AGENT_USER 
       foreign key (dc_user) 
       references core_user (dc_id) 
       on delete cascade;

    alter table pd_agent 
       add constraint FK_AGENT_SERVICES 
       foreign key (pd_services) 
       references pd_service (dc_id) 
       on delete cascade;

    alter table pd_cache 
       add constraint FK_RECORD_GROUP 
       foreign key (pd_group) 
       references pd_group (dc_id) 
       on delete cascade;

    alter table pd_cache_value 
       add constraint FK_CACHE_VALUE_AGENT 
       foreign key (pd_agent) 
       references pd_agent (dc_id);

    alter table pd_cache_value 
       add constraint FK_CACHE_VALUE_RECORD 
       foreign key (pd_record) 
       references pd_cache (dc_id);

    alter table pd_group 
       add constraint FK_GROUP_SERVICES 
       foreign key (pd_services) 
       references pd_service (dc_id) 
       on delete cascade;

    alter table pd_query 
       add constraint FK_QUERY_SERVICES 
       foreign key (pd_services) 
       references pd_service (dc_id) 
       on delete cascade;

    alter table pd_ref_agent_group 
       add constraint FK_GROUP_AGENT 
       foreign key (agent_id) 
       references pd_agent (dc_id);

    alter table pd_ref_agent_group 
       add constraint FK_AGENT_GROUP 
       foreign key (group_id) 
       references pd_group (dc_id) 
       on delete cascade;
