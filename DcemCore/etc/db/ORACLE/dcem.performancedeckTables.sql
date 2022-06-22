create sequence pd_agent_seq start with 1 increment by  4;
create sequence pd_group_seq start with 1 increment by  4;
create sequence pd_query_seq start with 1 increment by  4;
create sequence pd_service_seq start with 1 increment by  4;

    create table core_action (
       dc_id number(10,0) not null,
        action varchar2(128 char) not null,
        moduleId varchar2(64 char) not null,
        subject varchar2(128 char) not null,
        primary key (dc_id)
    );

    create table core_group (
       dc_id number(10,0) not null,
        jpaVersion number(10,0) not null,
        description varchar2(255 char),
        groupDn varchar2(255 char),
        dc_name varchar2(255 char) not null,
        dc_role number(10,0),
        dc_ldap number(10,0),
        primary key (dc_id)
    );

    create table core_ldap (
       dc_id number(10,0) not null,
        baseDN varchar2(255 char) not null,
        configJson long,
        domainType number(10,0) not null,
        enable number(1,0) not null,
        filter varchar2(255 char) not null,
        firstNameAttribute varchar2(255 char) not null,
        host varchar2(255 char) not null,
        lastNameAttribute varchar2(255 char) not null,
        loginAttribute varchar2(255 char) not null,
        mailAttribute varchar2(255 char),
        mapEmailDomains varchar2(255 char),
        mobileAttribute varchar2(255 char),
        name varchar2(64 char) not null,
        password long raw not null,
        dc_rank number(10,0),
        searchAccount varchar2(255 char) not null,
        telephoneAttribute varchar2(255 char),
        dc_version number(10,0),
        primary key (dc_id)
    );

    create table core_ref_user_group (
       group_id number(10,0) not null,
        user_id number(10,0) not null
    );

    create table core_role (
       dc_id number(10,0) not null,
        disabled number(1,0) not null,
        jpaVersion number(10,0) not null,
        dc_name varchar2(64 char) not null,
        dc_rank number(10,0) not null,
        systemRole number(1,0) not null,
        primary key (dc_id)
    );

    create table core_role_core_action (
       core_role_dc_id number(10,0) not null,
        actions_dc_id number(10,0) not null,
        primary key (core_role_dc_id, actions_dc_id)
    );

    create table core_seq (
       seq_name varchar2(255 char) not null,
        seq_value number(19,0),
        primary key (seq_name)
    );

    insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id number(10,0) not null,
        acSuspendedTill timestamp,
        disabled number(1,0) not null,
        displayName varchar2(128 char),
        email varchar2(255 char),
        failActivations number(10,0) not null,
        hashPassword long raw,
        hmac raw(32) not null,
        jpaVersion number(10,0) not null,
        locale number(10,0),
        lastLogin timestamp,
        loginId varchar2(64 char) not null,
        mobileNumber varchar2(255 char),
        objectGuid raw(255),
        passCounter number(10,0) not null,
        prvMobile varchar2(32 char),
        dc_salt raw(32),
        saveit long raw,
        dc_tel varchar2(255 char),
        userDn varchar2(255 char),
        userPrincipalName varchar2(128 char),
        dc_role number(10,0) not null,
        dc_ldap number(10,0),
        primary key (dc_id)
    );

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
