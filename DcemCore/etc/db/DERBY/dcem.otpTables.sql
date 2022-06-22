
    create table core_action (
       dc_id integer not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
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

    insert into core_seq(seq_name, seq_value) values ('OPT_TOKEN.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_user (
       dc_id integer not null,
        acSuspendedTill timestamp,
        disabled boolean not null,
        displayName varchar(255),
        email varchar(255),
        failActivations integer not null,
        hashPassword long varchar for bit data,
        hmac varchar(32) for bit data not null,
        jpaVersion integer not null,
        locale integer,
        lastLogin timestamp,
        loginId varchar(255) not null,
        mobileNumber varchar(255),
        objectGuid varchar(255) for bit data,
        passCounter integer not null,
        privateEmail varchar(255),
        prvMobile varchar(32),
        dc_salt varchar(32) for bit data,
        saveit long varchar for bit data,
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(255),
        dc_role integer not null,
        userext integer,
        dc_ldap integer,
        primary key (dc_id)
    );

    create table core_userext (
       dc_userext_id integer not null,
        dc_country varchar(255),
        photo varchar(8096) for bit data,
        dc_timezone varchar(255),
        primary key (dc_userext_id)
    );

    create table otp_token (
       dc_id integer not null,
        counter integer not null,
        dc_disabled boolean,
        info varchar(255),
        otpType integer not null,
        secretKey long varchar for bit data not null,
        serialNumber varchar(255) not null,
        userId integer,
        primary key (dc_id)
    );
create unique index UK_SEM_ACTION on core_action (moduleId, subject, action);
create unique index UK_LDAP_NAME on core_ldap (name);
create unique index UK_ROLE_NAME on core_role (dc_name);
create unique index UK_APP_USER on core_user (loginId);
create unique index UK_OTP_SERIAL on otp_token (serialNumber);

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
