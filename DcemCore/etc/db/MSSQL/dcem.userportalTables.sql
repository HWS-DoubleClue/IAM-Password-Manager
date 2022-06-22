
    create table core_action (
       dc_id int not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
        primary key (dc_id)
    );

    create table core_group (
       dc_id int not null,
        jpaVersion int not null,
        description varchar(255),
        groupDn varchar(255),
        dc_name varchar(255) not null,
        dc_role int,
        dc_ldap int,
        primary key (dc_id)
    );

    create table core_ldap (
       dc_id int not null,
        baseDN varchar(255) not null,
        configJson varchar(4096),
        domainType int not null,
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
        password varbinary(MAX) not null,
        dc_rank int,
        searchAccount varchar(255) not null,
        telephoneAttribute varchar(255),
        dc_version int,
        primary key (dc_id)
    );

    create table core_ref_user_group (
       group_id int not null,
        user_id int not null
    );

    create table core_role (
       dc_id int not null,
        disabled bit not null,
        jpaVersion int not null,
        dc_name varchar(64) not null,
        dc_rank int not null,
        systemRole bit not null,
        primary key (dc_id)
    );

    create table core_role_core_action (
       core_role_dc_id int not null,
        actions_dc_id int not null,
        primary key (core_role_dc_id, actions_dc_id)
    );

    create table core_seq (
       seq_name varchar(255) not null,
        seq_value bigint,
        primary key (seq_name)
    );

    insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id int not null,
        acSuspendedTill datetime2,
        disabled bit not null,
        displayName varchar(128),
        email varchar(255),
        failActivations int not null,
        hashPassword varbinary(MAX),
        hmac varbinary(32) not null,
        jpaVersion int not null,
        locale int,
        lastLogin datetime2,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        objectGuid varbinary(255),
        passCounter int not null,
        prvMobile varchar(32),
        dc_salt varbinary(32),
        saveit varbinary(MAX),
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(128),
        dc_role int not null,
        dc_ldap int,
        primary key (dc_id)
    );

    create table up_apphub_group (
       apphub_up_id int not null,
        group_dc_id int not null
    );

    create table up_apphubseq (
       next_val bigint
    );

    insert into up_apphubseq values ( 1 );

    create table up_applicationhub (
       up_id int not null,
        application varchar(MAX) not null,
        included bit not null,
        logo varbinary(MAX),
        up_name varchar(255) not null,
        primary key (up_id)
    );

    create table up_applicationhubdashboard (
       up_application int not null,
        up_user int not null,
        up_index int,
        primary key (up_application, up_user)
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
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table up_apphub_group 
       add constraint FK_APPHUB_GROUP 
       foreign key (group_dc_id) 
       references core_group;

    alter table up_apphub_group 
       add constraint FK1wkg76ssvji8w1pag3w0atcw4 
       foreign key (apphub_up_id) 
       references up_applicationhub;

    alter table up_applicationhubdashboard 
       add constraint FK_REF_APPHUB 
       foreign key (up_application) 
       references up_applicationhub;

    alter table up_applicationhubdashboard 
       add constraint FK_REF_USER 
       foreign key (up_user) 
       references core_user;
