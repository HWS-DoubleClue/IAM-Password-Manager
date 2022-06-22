create sequence up_apphubseq start with 1 increment by  4;

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

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id number(10,0) not null,
        acSuspendedTill timestamp,
        disabled number(1,0) not null,
        displayName varchar2(255 char),
        email varchar2(255 char),
        failActivations number(10,0) not null,
        hashPassword long raw,
        hmac raw(32) not null,
        jpaVersion number(10,0) not null,
        locale number(10,0),
        lastLogin timestamp,
        loginId varchar2(255 char) not null,
        mobileNumber varchar2(255 char),
        objectGuid raw(255),
        passCounter number(10,0) not null,
        privateEmail varchar2(255 char),
        prvMobile varchar2(32 char),
        dc_salt raw(32),
        saveit long raw,
        dc_tel varchar2(255 char),
        userDn varchar2(255 char),
        userPrincipalName varchar2(255 char),
        dc_role number(10,0) not null,
        userext number(10,0),
        dc_ldap number(10,0),
        primary key (dc_id)
    );

    create table core_userext (
       dc_userext_id number(10,0) not null,
        dc_country varchar2(255 char),
        photo long raw,
        dc_timezone varchar2(255 char),
        primary key (dc_userext_id)
    );

    create table up_applicationhub (
       up_id number(10,0) not null,
        application long not null,
        logo long raw,
        up_name varchar2(255 char) not null,
        primary key (up_id)
    );

    create table up_keepassentry (
       dc_id varchar2(255 char) not null,
        application long,
        up_name varchar2(255 char) not null,
        appEntity number(10,0),
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

    alter table up_applicationhub 
       add constraint UK_APPHUB_NAME unique (up_name);

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
       add constraint FK_USER_EXTENSION 
       foreign key (userext) 
       references core_userext;

    alter table core_user 
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table up_keepassentry 
       add constraint FK_KEEPASS_APP 
       foreign key (appEntity) 
       references up_applicationhub;
