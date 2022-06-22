
    create table core_action (
       dc_id integer not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
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

    insert into core_seq(seq_name, seq_value) values ('OPT_TOKEN.ID',1);

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

    create table otp_token (
       dc_id integer not null,
        counter integer not null,
        dc_disabled bit,
        info varchar(255),
        otpType integer not null,
        secretKey mediumblob not null,
        serialNumber varchar(255) not null,
        userId integer,
        primary key (dc_id)
    ) engine=InnoDB;

    alter table core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table core_user 
       add constraint UK_APP_USER unique (loginId);

    alter table otp_token 
       add constraint UK_OTP_SERIAL unique (serialNumber);

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

    alter table otp_token 
       add constraint FK_OTP_TOKEN_USER 
       foreign key (userId) 
       references core_user (dc_id);
