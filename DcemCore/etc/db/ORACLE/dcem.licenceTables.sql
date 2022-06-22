
    create table core_action (
       dc_id number(10,0) not null,
        action varchar2(128 char) not null,
        moduleId varchar2(64 char) not null,
        subject varchar2(128 char) not null,
        primary key (dc_id)
    );

    create table core_ldap (
       dc_id number(10,0) not null,
        baseDN varchar2(255 char) not null,
        domainType number(10,0) not null,
        enable number(1,0) not null,
        filter varchar2(255 char) not null,
        firstNameAttribute varchar2(255 char) not null,
        host varchar2(255 char) not null,
        lastNameAttribute varchar2(255 char) not null,
        loginAttribute varchar2(255 char) not null,
        mailAttribute varchar2(255 char),
        mobileAttribute varchar2(255 char),
        name varchar2(64 char) not null,
        password long raw not null,
        port number(10,0) not null,
        searchAccount varchar2(255 char) not null,
        secure number(1,0) not null,
        telephoneAttribute varchar2(255 char),
        timeout number(10,0),
        dc_version number(10,0),
        primary key (dc_id)
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

    insert into core_seq(seq_name, seq_value) values ('LICENCE_CLUSTER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CUSTOMER_CLIENT.ID',1);

    insert into core_seq(seq_name, seq_value) values ('OPT_TOKEN.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LICENCE_ORDER.ID',1);

    create table core_user (
       dc_id number(10,0) not null,
        acSuspendedTill timestamp,
        disabled number(1,0) not null,
        displayName varchar2(128 char),
        email varchar2(255 char),
        failActivations number(10,0) not null,
        hashPassword long raw,
        initialPassword raw(128),
        jpaVersion number(10,0) not null,
        locale number(10,0),
        lastLogin timestamp,
        loginId varchar2(64 char) not null,
        mobileNumber varchar2(255 char),
        passCounter number(10,0) not null,
        prvMobile varchar2(32 char),
        saveit long raw,
        dc_tel varchar2(255 char),
        userDn varchar2(255 char),
        dc_role number(10,0) not null,
        dc_ldap number(10,0),
        primary key (dc_id)
    );

    create table licence_cluster (
       dc_id number(10,0) not null,
        clusterId varchar2(255 char) not null,
        clusterName varchar2(255 char) not null,
        createdOn timestamp not null,
        dc_disabled number(1,0) not null,
        information varchar2(255 char),
        productive number(1,0),
        dc_customer number(10,0) not null,
        primary key (dc_id)
    );

    create table licence_customer (
       dc_id number(10,0) not null,
        adress varchar2(255 char) not null,
        contact_email varchar2(255 char),
        contact_name varchar2(255 char),
        country varchar2(128 char) not null,
        name varchar2(255 char) not null,
        zip_code varchar2(255 char),
        primary key (dc_id)
    );

    create table licence_orders (
       dc_id number(10,0) not null,
        lastModified timestamp not null,
        limitationMap long not null,
        moduleID varchar2(255 char),
        tenantID varchar2(255 char),
        clusterId number(10,0) not null,
        customerId number(10,0) not null,
        lastModifiedBy number(10,0) not null,
        primary key (dc_id)
    );

    create table licence_otp_token (
       dc_id number(10,0) not null,
        otpType number(10,0) not null,
        secretKey long raw not null,
        serialNumber varchar2(255 char) not null,
        dc_customer number(10,0),
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
