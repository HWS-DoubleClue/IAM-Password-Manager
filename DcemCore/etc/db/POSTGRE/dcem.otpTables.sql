
    create table core_action (
       dc_id int4 not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
        primary key (dc_id)
    );

    create table core_department (
       dc_id  bigserial not null,
        abbriviation varchar(255),
        dc_desc varchar(255),
        dc_name varchar(255) not null,
        deputy_dc_id int4,
        headOf_dc_id int4 not null,
        dc_parent_id int8,
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

    insert into core_seq(seq_name, seq_value) values ('OPT_TOKEN.ID',1);

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
        dc_id int8,
        primary key (dc_userext_id)
    );

    create table otp_token (
       dc_id int4 not null,
        counter int4 not null,
        dc_disabled boolean,
        info varchar(255),
        otpType int4 not null,
        secretKey bytea not null,
        serialNumber varchar(255) not null,
        userId int4,
        primary key (dc_id)
    );

    alter table if exists core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table if exists core_department 
       add constraint UK_DEPARTMENT_NAME unique (dc_name);

    alter table if exists core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table if exists core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table if exists core_user 
       add constraint UK_APP_USER unique (loginId);

    alter table if exists otp_token 
       add constraint UK_OTP_SERIAL unique (serialNumber);

    alter table if exists core_department 
       add constraint FK_APP_DEPARTMENT_USER_DEPUTY 
       foreign key (deputy_dc_id) 
       references core_user;

    alter table if exists core_department 
       add constraint FK_APP_DEPARTMENT_USER 
       foreign key (headOf_dc_id) 
       references core_user;

    alter table if exists core_department 
       add constraint FK_DEPARTMENT_PARENT_ID 
       foreign key (dc_parent_id) 
       references core_department;

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

    alter table if exists core_userext 
       add constraint FK_DEPARTMENT_USEREXT_ID 
       foreign key (dc_id) 
       references core_department;

    alter table if exists otp_token 
       add constraint FK_OTP_TOKEN_USER 
       foreign key (userId) 
       references core_user;
