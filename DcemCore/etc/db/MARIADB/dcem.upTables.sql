
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

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id integer not null,
        acSuspendedTill datetime,
        disabled bit not null,
        displayName varchar(255),
        email varchar(255),
        failActivations integer not null,
        hashPassword mediumblob,
        hmac tinyblob not null,
        jpaVersion integer not null,
        locale integer,
        lastLogin datetime,
        loginId varchar(255) not null,
        mobileNumber varchar(255),
        objectGuid tinyblob,
        passCounter integer not null,
        privateEmail varchar(255),
        prvMobile varchar(32),
        dc_salt tinyblob,
        saveit mediumblob,
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(255),
        dc_role integer not null,
        userext integer,
        dc_ldap integer,
        primary key (dc_id)
    ) engine=InnoDB;

    create table core_userext (
       dc_userext_id integer not null,
        dc_country varchar(255),
        photo blob,
        dc_timezone varchar(255),
        primary key (dc_userext_id)
    ) engine=InnoDB;

    create table up_apphubseq (
       next_val bigint
    ) engine=InnoDB;

    insert into up_apphubseq values ( 1 );

    create table up_applicationhub (
       up_id integer not null,
        application varchar(10000) not null,
        logo blob,
        up_name varchar(255) not null,
        primary key (up_id)
    ) engine=InnoDB;

    create table up_keepassentry (
       dc_id varchar(255) not null,
        application varchar(10000),
        up_name varchar(255) not null,
        appEntity integer,
        primary key (dc_id)
    ) engine=InnoDB;

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
       add constraint FK_USER_EXTENSION 
       foreign key (userext) 
       references core_userext (dc_userext_id);

    alter table core_user 
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap (dc_id);

    alter table up_keepassentry 
       add constraint FK_KEEPASS_APP 
       foreign key (appEntity) 
       references up_applicationhub (up_id);
