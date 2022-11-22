
    create table appo_timeEvent (
       id  bigserial not null,
        tp_end timestamp,
        tp_start timestamp,
        userInfo varchar(4096),
        userappo int4 not null,
        timeEvent int8 not null,
        primary key (id)
    );

    create table appo_timetable (
       dc_id  bigserial not null,
        dc_description varchar(255),
        dc_enabled boolean,
        dc_name varchar(255),
        slotTimeMinutes int4 not null,
        primary key (dc_id)
    );

    create table appo_timetableEvent (
       dc_id  bigserial not null,
        dc_end timestamp,
        dc_start timestamp,
        timeEvent int8 not null,
        primary key (dc_id)
    );

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

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id int4 not null,
        acSuspendedTill timestamp,
        disabled boolean not null,
        displayName varchar(255),
        email varchar(255),
        failActivations int4 not null,
        hashPassword bytea,
        hmac bytea not null,
        jpaVersion int4 not null,
        locale int4,
        lastLogin timestamp,
        loginId varchar(255) not null,
        mobileNumber varchar(255),
        objectGuid bytea,
        passCounter int4 not null,
        privateEmail varchar(255),
        prvMobile varchar(32),
        dc_salt bytea,
        saveit bytea,
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(255),
        dc_role int4 not null,
        userext int4,
        dc_ldap int4,
        primary key (dc_id)
    );

    create table core_userext (
       dc_userext_id int4 not null,
        dc_country varchar(255),
        photo bytea,
        dc_timezone varchar(255),
        primary key (dc_userext_id)
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

    alter table if exists appo_timeEvent 
       add constraint FK_USER_APPOINTMENTS 
       foreign key (userappo) 
       references core_user;

    alter table if exists appo_timeEvent 
       add constraint FK_EVENT_TIMETABLE 
       foreign key (timeEvent) 
       references appo_timetableEvent;

    alter table if exists appo_timetableEvent 
       add constraint FK_EVENT_TIMETABLE 
       foreign key (timeEvent) 
       references appo_timetable;

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
       add constraint FK_USER_EXTENSION 
       foreign key (userext) 
       references core_userext;

    alter table if exists core_user 
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;
