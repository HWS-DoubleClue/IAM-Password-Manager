
    create table core_action (
       dc_id int4 not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
        primary key (dc_id)
    );

    create table core_ldap (
       dc_id int4 not null,
        baseDN varchar(255) not null,
        domainType int4 not null,
        enable boolean not null,
        filter varchar(255) not null,
        firstNameAttribute varchar(255) not null,
        host varchar(255) not null,
        lastNameAttribute varchar(255) not null,
        loginAttribute varchar(255) not null,
        mailAttribute varchar(255),
        mobileAttribute varchar(255),
        name varchar(64) not null,
        password bytea not null,
        port int4 not null,
        searchAccount varchar(255) not null,
        secure boolean not null,
        telephoneAttribute varchar(255),
        timeout int4,
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

    insert into core_seq(seq_name, seq_value) values ('LICENCE_CLUSTER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CUSTOMER_CLIENT.ID',1);

    insert into core_seq(seq_name, seq_value) values ('OPT_TOKEN.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LICENCE_ORDER.ID',1);

    create table core_user (
       dc_id int4 not null,
        acSuspendedTill timestamp,
        disabled boolean not null,
        displayName varchar(128),
        email varchar(255),
        failActivations int4 not null,
        hashPassword bytea,
        initialPassword bytea,
        jpaVersion int4 not null,
        locale int4,
        lastLogin timestamp,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        passCounter int4 not null,
        prvMobile varchar(32),
        saveit bytea,
        dc_tel varchar(255),
        userDn varchar(255),
        dc_role int4 not null,
        dc_ldap int4,
        primary key (dc_id)
    );

    create table licence_cluster (
       dc_id int4 not null,
        clusterId varchar(255) not null,
        clusterName varchar(255) not null,
        createdOn timestamp not null,
        dc_disabled boolean not null,
        information varchar(255),
        productive boolean,
        dc_customer int4 not null,
        primary key (dc_id)
    );

    create table licence_customer (
       dc_id int4 not null,
        adress varchar(255) not null,
        contact_email varchar(255),
        contact_name varchar(255),
        country varchar(128) not null,
        name varchar(255) not null,
        zip_code varchar(255),
        primary key (dc_id)
    );

    create table licence_orders (
       dc_id int4 not null,
        lastModified timestamp not null,
        limitationMap varchar(4096) not null,
        moduleID varchar(255),
        tenantID varchar(255),
        clusterId int4 not null,
        customerId int4 not null,
        lastModifiedBy int4 not null,
        primary key (dc_id)
    );

    create table licence_otp_token (
       dc_id int4 not null,
        otpType int4 not null,
        secretKey bytea not null,
        serialNumber varchar(255) not null,
        dc_customer int4,
        primary key (dc_id)
    );

    alter table if exists core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table if exists core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table if exists core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table if exists core_user 
       add constraint UK_APP_USER unique (loginId);

    alter table if exists licence_cluster 
       add constraint UK_LICENCE_CLUSTER_ID unique (clusterId);

    alter table if exists licence_cluster 
       add constraint UK_LICENCE_CLUSTER_NAME unique (clusterName);

    alter table if exists licence_customer 
       add constraint UK_CUSTOMER_NAME unique (name);

    alter table if exists licence_otp_token 
       add constraint UK_LICENCE_OTP_SERIAL unique (serialNumber);

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
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table if exists licence_cluster 
       add constraint FK_CLUSTER_CUSTOMER 
       foreign key (dc_customer) 
       references licence_customer;

    alter table if exists licence_orders 
       add constraint FK_licence_orders_license_cluster_id 
       foreign key (clusterId) 
       references licence_cluster;

    alter table if exists licence_orders 
       add constraint FK_licence_orders_license_customer_id 
       foreign key (customerId) 
       references licence_customer;

    alter table if exists licence_orders 
       add constraint FK_license_orders_core_user_id 
       foreign key (lastModifiedBy) 
       references core_user;

    alter table if exists licence_otp_token 
       add constraint FK_OTP_CUSTOMER 
       foreign key (dc_customer) 
       references licence_customer;
