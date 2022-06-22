
    create table core_action (
       dc_id int not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
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

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('OPT_TOKEN.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id int not null,
        acSuspendedTill datetime2,
        disabled bit not null,
        displayName varchar(255),
        email varchar(255),
        failActivations int not null,
        hashPassword varbinary(MAX),
        hmac varbinary(32) not null,
        jpaVersion int not null,
        locale int,
        lastLogin datetime2,
        loginId varchar(255) not null,
        mobileNumber varchar(255),
        objectGuid varbinary(255),
        passCounter int not null,
        privateEmail varchar(255),
        prvMobile varchar(32),
        dc_salt varbinary(32),
        saveit varbinary(MAX),
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(255),
        dc_role int not null,
        userext int,
        dc_ldap int,
        primary key (dc_id)
    );

    create table core_userext (
       dc_userext_id int not null,
        dc_country varchar(255),
        photo varbinary(MAX),
        dc_timezone varchar(255),
        primary key (dc_userext_id)
    );

    create table otp_token (
       dc_id int not null,
        counter int not null,
        dc_disabled bit,
        info varchar(255),
        otpType int not null,
        secretKey varbinary(MAX) not null,
        serialNumber varchar(255) not null,
        userId int,
        primary key (dc_id)
    );

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

    alter table otp_token 
       add constraint FK_OTP_TOKEN_USER 
       foreign key (userId) 
       references core_user;
