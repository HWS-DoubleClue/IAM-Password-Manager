
    create table as_activationcode (
       dc_id int not null,
        activationCode varbinary(MAX) not null,
        createdOn datetime2 not null,
        info varchar(255),
        validTill datetime2 not null,
        userId int not null,
        primary key (dc_id)
    );

    create table as_app_policy_group (
       dc_id int not null,
        dc_priority int,
        group_id int,
        policyApp_id int not null,
        policy_id int,
        primary key (dc_id)
    );

    create table as_authApp (
       dc_id int not null,
        disabled bit not null,
        dc_name varchar(64),
        retryCounter int not null,
        sharedKey varbinary(MAX),
        primary key (dc_id)
    );

    create table as_cloudsafe (
       dc_id int not null,
        discardAfter datetime2,
        dc_is_folder bit not null,
        dc_gcm bit not null,
        lastModified datetime2,
        dc_length bigint,
        dc_name varchar(255) not null,
        options varchar(255),
        owner int,
        recycled bit not null,
        dc_salt varbinary(32),
        device_dc_id int not null,
        group_dc_id int,
        lastModifiedUser_dc_id int,
        dc_parent_id int,
        user_dc_id int not null,
        primary key (dc_id)
    );

    create table as_cloudsafecontent (
       cloudDataEntity_dc_id int not null,
        content varbinary(MAX),
        primary key (cloudDataEntity_dc_id)
    );

    create table as_cloudsafelimit (
       expiry_date datetime2,
        dc_limit bigint not null,
        ps_enabled bit not null,
        dc_used bigint not null,
        user_dc_id int not null,
        primary key (user_dc_id)
    );

    create table as_cloudsafeshare (
       dc_id int not null,
        restrictDownload bit,
        writeAccess bit,
        cloudSafe_dc_id int,
        group_dc_id int,
        user_dc_id int,
        primary key (dc_id)
    );

    create table as_device (
       dc_id int not null,
        appOsVersion varchar(255),
        deviceHash varbinary(255),
        deviceKey varbinary(MAX),
        lastLogin datetime2,
        locale varchar(2),
        manufacture varchar(255),
        name varchar(64),
        nodeId int,
        offlineCounter int not null,
        offlineKey varbinary(MAX),
        publicKey varbinary(1024),
        retryCounter int not null,
        risks varchar(255),
        dc_state int not null,
        dc_status int not null,
        udid varbinary(255),
        asVersion_dc_id int,
        userId int not null,
        primary key (dc_id)
    );

    create table as_fido_authenticator (
       dc_id int not null,
        credentialId varchar(255) not null,
        display_name varchar(255) not null,
        lastUsed datetime2 not null,
        passwordless bit not null,
        publicKey varbinary(1024) not null,
        registeredOn datetime2 not null,
        userId int not null,
        primary key (dc_id)
    );

    create table as_message (
       dc_id bigint not null,
        actionId varchar(64),
        createdOn datetime2 not null,
        info varchar(255),
        dc_status int not null,
        outputData varchar(4096),
        responseData varchar(4096),
        responseRequired bit not null,
        retrieved bit not null,
        signature varbinary(255),
        signed bit not null,
        device_dc_id int,
        operatorId int,
        policyAppId int,
        template_dc_id int,
        userId int not null,
        primary key (dc_id)
    );

    create table as_policy (
       dc_id int not null,
        jsonPolicy varchar(4096),
        dc_name varchar(255),
        primary key (dc_id)
    );

    create table as_policy_app (
       dc_id int not null,
        authapp varchar(255),
        dc_disabled bit,
        subId int not null,
        subname varchar(255),
        primary key (dc_id)
    );

    create table as_userfingerprint (
       policyAppId int not null,
        userId int not null,
        fingerprint varchar(255),
        timeStamp datetime2,
        primary key (policyAppId, userId)
    );

    create table as_version (
       dc_id int not null,
        clientType int,
        dc_disabled bit,
        downloadUrl varchar(255),
        expiresOn datetime2,
        informationUrl varchar(255),
        jpaVersion int not null,
        dc_name varchar(128),
        testApp bit,
        as_version int,
        versionStr varchar(128),
        user_dc_id int,
        primary key (dc_id)
    );

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

    insert into core_seq(seq_name, seq_value) values ('APP_AUTHAPP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('POLICYAPP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('AS_APP_POLICY_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('AS_CLOUDDATASHARE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('APP_AC.ID',1);

    insert into core_seq(seq_name, seq_value) values ('NODE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('APP_TEMPLATE_ID',1);

    insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('APP_VERSION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('FIDO.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('AS_CLOUDDATA.ID',1);

    insert into core_seq(seq_name, seq_value) values ('APP_DEVICE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('POLICY.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    create table core_template (
       dc_id int not null,
        active bit not null,
        content varchar(MAX),
        defaultTemplate bit not null,
        inUse bit not null,
        jpaVersion int not null,
        language int not null,
        lastModified datetime2,
        macDigest varbinary(32),
        dc_name varchar(128) not null,
        dc_tokens varchar(4096),
        dc_version int,
        primary key (dc_id)
    );

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

    create table sys_node (
       dc_id int not null,
        dc_name varchar(64) not null,
        startedOn datetime2,
        state int not null,
        wentDownOn datetime2,
        primary key (dc_id)
    );

    alter table as_authApp 
       add constraint UK_AUTHAPP_NAME unique (dc_name);

    alter table as_cloudsafe 
       add constraint UK_AS_CLOUDDATA unique (dc_name, owner, user_dc_id, device_dc_id, dc_parent_id, group_dc_id);
create index IDX_DEVICE_LAST_LOGIN on as_device (lastLogin, dc_state);
create index IDX_DEVICE_USER on as_device (userId);

    alter table as_device 
       add constraint UK_DEVICE_USER unique (userId, name);
create index FIDO_AUTH_CREDENTIAL_ID_INDEX on as_fido_authenticator (credentialId);

    alter table as_fido_authenticator 
       add constraint UK_USER_CREDENTIAL_ID unique (userId, credentialId);

    alter table as_policy 
       add constraint UK_POLICY_NAME unique (dc_name);

    alter table as_policy_app 
       add constraint UK_POLICY_APP unique (authapp, subId);

    alter table as_version 
       add constraint UK_VERSION_NAME_TYPE unique (dc_name, versionStr, clientType);

    alter table core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table core_group 
       add constraint UK_APP_GROUP unique (dc_name);

    alter table core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table core_template 
       add constraint UK_APP_TEMPLATE unique (dc_name, language, dc_version);

    alter table core_user 
       add constraint UK_APP_USER unique (loginId);

    alter table sys_node 
       add constraint UK_NODE_NAME unique (dc_name);

    alter table as_activationcode 
       add constraint FK_APP_AC_USER 
       foreign key (userId) 
       references core_user;

    alter table as_app_policy_group 
       add constraint FK_REF_GROUP 
       foreign key (group_id) 
       references core_group;

    alter table as_app_policy_group 
       add constraint FK_REF_APP_POLICY 
       foreign key (policyApp_id) 
       references as_policy_app;

    alter table as_app_policy_group 
       add constraint FK_REF_POLICY 
       foreign key (policy_id) 
       references as_policy;

    alter table as_cloudsafe 
       add constraint FK_AS_PROP_DEVICE 
       foreign key (device_dc_id) 
       references as_device;

    alter table as_cloudsafe 
       add constraint FK_AS_PROP_GROUP 
       foreign key (group_dc_id) 
       references core_group;

    alter table as_cloudsafe 
       add constraint FK_AS_PROP_USER_MODIFIED 
       foreign key (lastModifiedUser_dc_id) 
       references core_user;

    alter table as_cloudsafe 
       add constraint FK_AS_PARENT_ID 
       foreign key (dc_parent_id) 
       references as_cloudsafe;

    alter table as_cloudsafe 
       add constraint FK_AS_PROP_USER 
       foreign key (user_dc_id) 
       references core_user;

    alter table as_cloudsafelimit 
       add constraint FK_CLOUDSAFE_LIMIT 
       foreign key (user_dc_id) 
       references core_user;

    alter table as_cloudsafeshare 
       add constraint FK_AS_CD_SHARE 
       foreign key (cloudSafe_dc_id) 
       references as_cloudsafe 
       on delete cascade;

    alter table as_cloudsafeshare 
       add constraint FK_AS_CD_GROUP 
       foreign key (group_dc_id) 
       references core_group;

    alter table as_cloudsafeshare 
       add constraint FK_AS_CD_SHARE_USER 
       foreign key (user_dc_id) 
       references core_user;

    alter table as_device 
       add constraint FK_APP_DEVICE_VERSION 
       foreign key (asVersion_dc_id) 
       references as_version;

    alter table as_device 
       add constraint FK_APP_DEVICE_USER 
       foreign key (userId) 
       references core_user;

    alter table as_fido_authenticator 
       add constraint FK_FIDO_USER 
       foreign key (userId) 
       references core_user;

    alter table as_message 
       add constraint FK_APP_MSG_DEVICE 
       foreign key (device_dc_id) 
       references as_device;

    alter table as_message 
       add constraint FK_MSG_OPERATOR 
       foreign key (operatorId) 
       references core_user;

    alter table as_message 
       add constraint FK_MSG_POLICYAPP 
       foreign key (policyAppId) 
       references as_policy_app;

    alter table as_message 
       add constraint FK_APP_MSG_TEMPLATE 
       foreign key (template_dc_id) 
       references core_template;

    alter table as_message 
       add constraint FK_APP_MSG_USER 
       foreign key (userId) 
       references core_user;

    alter table as_version 
       add constraint FK_APP_VERSION_USER 
       foreign key (user_dc_id) 
       references core_user;

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
       add constraint FK_USER_EXTENSION 
       foreign key (userext) 
       references core_userext;

    alter table core_user 
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;
