
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
        domainType integer not null,
        enable boolean not null,
        filter varchar(255) not null,
        firstNameAttribute varchar(255) not null,
        host varchar(255) not null,
        lastNameAttribute varchar(255) not null,
        loginAttribute varchar(255) not null,
        mailAttribute varchar(255),
        mobileAttribute varchar(255),
        name varchar(64) not null,
        password long varchar for bit data not null,
        port integer not null,
        searchAccount varchar(255) not null,
        secure boolean not null,
        telephoneAttribute varchar(255),
        timeout integer,
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

    insert into core_seq(seq_name, seq_value) values ('LICENCE_CLUSTER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CUSTOMER_CLIENT.ID',1);

    insert into core_seq(seq_name, seq_value) values ('OPT_TOKEN.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LICENCE_ORDER.ID',1);

    create table core_user (
       dc_id integer not null,
        acSuspendedTill timestamp,
        disabled boolean not null,
        displayName varchar(128),
        email varchar(255),
        failActivations integer not null,
        hashPassword long varchar for bit data,
        initialPassword varchar(128) for bit data,
        jpaVersion integer not null,
        locale integer,
        lastLogin timestamp,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        passCounter integer not null,
        prvMobile varchar(32),
        saveit long varchar for bit data,
        dc_tel varchar(255),
        userDn varchar(255),
        dc_role integer not null,
        dc_ldap integer,
        primary key (dc_id)
    );

    create table licence_cluster (
       dc_id integer not null,
        clusterId varchar(255) not null,
        clusterName varchar(255) not null,
        createdOn timestamp not null,
        dc_disabled boolean not null,
        information varchar(255),
        productive boolean,
        dc_customer integer not null,
        primary key (dc_id)
    );

    create table licence_customer (
       dc_id integer not null,
        adress varchar(255) not null,
        contact_email varchar(255),
        contact_name varchar(255),
        country varchar(128) not null,
        name varchar(255) not null,
        zip_code varchar(255),
        primary key (dc_id)
    );

    create table licence_orders (
       dc_id integer not null,
        lastModified timestamp not null,
        limitationMap varchar(4096) not null,
        moduleID varchar(255),
        tenantID varchar(255),
        clusterId integer not null,
        customerId integer not null,
        lastModifiedBy integer not null,
        primary key (dc_id)
    );

    create table licence_otp_token (
       dc_id integer not null,
        otpType integer not null,
        secretKey long varchar for bit data not null,
        serialNumber varchar(255) not null,
        dc_customer integer,
        primary key (dc_id)
    );
create unique index UK_SEM_ACTION on core_action (moduleId, subject, action);
create unique index UK_LDAP_NAME on core_ldap (name);
create unique index UK_ROLE_NAME on core_role (dc_name);
create unique index UK_APP_USER on core_user (loginId);
create unique index UK_LICENCE_CLUSTER_ID on licence_cluster (clusterId);
create unique index UK_LICENCE_CLUSTER_NAME on licence_cluster (clusterName);
create unique index UK_CUSTOMER_NAME on licence_customer (name);
create unique index UK_LICENCE_OTP_SERIAL on licence_otp_token (serialNumber);

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

    alter table licence_cluster 
       add constraint FK_CLUSTER_CUSTOMER 
       foreign key (dc_customer) 
       references licence_customer;

    alter table licence_orders 
       add constraint FK_licence_orders_license_cluster_id 
       foreign key (clusterId) 
       references licence_cluster;

    alter table licence_orders 
       add constraint FK_licence_orders_license_customer_id 
       foreign key (customerId) 
       references licence_customer;

    alter table licence_orders 
       add constraint FK_license_orders_core_user_id 
       foreign key (lastModifiedBy) 
       references core_user;

    alter table licence_otp_token 
       add constraint FK_OTP_CUSTOMER 
       foreign key (dc_customer) 
       references licence_customer;
