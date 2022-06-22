
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
        domainType int not null,
        enable bit not null,
        filter varchar(255) not null,
        firstNameAttribute varchar(255) not null,
        host varchar(255) not null,
        lastNameAttribute varchar(255) not null,
        loginAttribute varchar(255) not null,
        mailAttribute varchar(255),
        mobileAttribute varchar(255),
        name varchar(64) not null,
        password varbinary(MAX) not null,
        port int not null,
        searchAccount varchar(255) not null,
        secure bit not null,
        telephoneAttribute varchar(255),
        timeout int,
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

    insert into core_seq(seq_name, seq_value) values ('LICENCE_CLUSTER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CUSTOMER_CLIENT.ID',1);

    insert into core_seq(seq_name, seq_value) values ('OPT_TOKEN.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LICENCE_ORDER.ID',1);

    create table core_user (
       dc_id int not null,
        acSuspendedTill datetime2,
        disabled bit not null,
        displayName varchar(128),
        email varchar(255),
        failActivations int not null,
        hashPassword varbinary(MAX),
        initialPassword varbinary(128),
        jpaVersion int not null,
        locale int,
        lastLogin datetime2,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        passCounter int not null,
        prvMobile varchar(32),
        saveit varbinary(MAX),
        dc_tel varchar(255),
        userDn varchar(255),
        dc_role int not null,
        dc_ldap int,
        primary key (dc_id)
    );

    create table licence_cluster (
       dc_id int not null,
        clusterId varchar(255) not null,
        clusterName varchar(255) not null,
        createdOn datetime2 not null,
        dc_disabled bit not null,
        information varchar(255),
        productive bit,
        dc_customer int not null,
        primary key (dc_id)
    );

    create table licence_customer (
       dc_id int not null,
        adress varchar(255) not null,
        contact_email varchar(255),
        contact_name varchar(255),
        country varchar(128) not null,
        name varchar(255) not null,
        zip_code varchar(255),
        primary key (dc_id)
    );

    create table licence_orders (
       dc_id int not null,
        lastModified datetime2 not null,
        limitationMap varchar(4096) not null,
        moduleID varchar(255),
        tenantID varchar(255),
        clusterId int not null,
        customerId int not null,
        lastModifiedBy int not null,
        primary key (dc_id)
    );

    create table licence_otp_token (
       dc_id int not null,
        otpType int not null,
        secretKey varbinary(MAX) not null,
        serialNumber varchar(255) not null,
        dc_customer int,
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

    alter table licence_cluster 
       add constraint UK_LICENCE_CLUSTER_ID unique (clusterId);

    alter table licence_cluster 
       add constraint UK_LICENCE_CLUSTER_NAME unique (clusterName);

    alter table licence_customer 
       add constraint UK_CUSTOMER_NAME unique (name);

    alter table licence_otp_token 
       add constraint UK_LICENCE_OTP_SERIAL unique (serialNumber);

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
