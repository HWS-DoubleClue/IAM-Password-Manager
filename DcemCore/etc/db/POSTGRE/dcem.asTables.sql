
    create table as_activationcode (
       dc_id int4 not null,
        activationCode bytea not null,
        createdOn timestamp not null,
        info varchar(255),
        validTill timestamp not null,
        userId int4 not null,
        primary key (dc_id)
    );

    create table as_app_policy_group (
       dc_id int4 not null,
        dc_priority int4,
        group_id int4,
        policyApp_id int4 not null,
        policy_id int4,
        primary key (dc_id)
    );

    create table as_authApp (
       dc_id int4 not null,
        disabled boolean not null,
        dc_name varchar(64),
        retryCounter int4 not null,
        sharedKey bytea,
        primary key (dc_id)
    );

    create table as_cloudsafe (
       dc_id int4 not null,
        discardAfter timestamp,
        dc_is_folder boolean not null,
        dc_gcm boolean not null,
        lastModified timestamp,
        dc_length int8,
        dc_name varchar(255) not null,
        options varchar(255),
        owner int4,
        recycled boolean not null,
        dc_salt bytea,
        device_dc_id int4 not null,
        group_dc_id int4,
        lastModifiedUser_dc_id int4,
        dc_parent_id int4,
        user_dc_id int4 not null,
        primary key (dc_id)
    );

    create table as_cloudsafecontent (
       cloudDataEntity_dc_id int4 not null,
        content oid,
        primary key (cloudDataEntity_dc_id)
    );

    create table as_cloudsafelimit (
       expiry_date timestamp,
        dc_limit int8 not null,
        ps_enabled boolean not null,
        dc_used int8 not null,
        user_dc_id int4 not null,
        primary key (user_dc_id)
    );

    create table as_cloudsafeshare (
       dc_id int4 not null,
        restrictDownload boolean,
        writeAccess boolean,
        cloudSafe_dc_id int4,
        group_dc_id int4,
        user_dc_id int4,
        primary key (dc_id)
    );

    create table as_device (
       dc_id int4 not null,
        appOsVersion varchar(255),
        deviceHash bytea,
        deviceKey bytea,
        lastLogin timestamp,
        locale varchar(2),
        manufacture varchar(255),
        name varchar(64),
        nodeId int4,
        offlineCounter int4 not null,
        offlineKey bytea,
        publicKey bytea,
        retryCounter int4 not null,
        risks varchar(255),
        dc_state int4 not null,
        dc_status int4 not null,
        udid bytea,
        asVersion_dc_id int4,
        userId int4 not null,
        primary key (dc_id)
    );

    create table as_fido_authenticator (
       dc_id int4 not null,
        credentialId varchar(255) not null,
        display_name varchar(255) not null,
        lastUsed timestamp not null,
        passwordless boolean not null,
        publicKey bytea not null,
        registeredOn timestamp not null,
        userId int4 not null,
        primary key (dc_id)
    );

    create table as_message (
       dc_id int8 not null,
        actionId varchar(64),
        createdOn timestamp not null,
        info varchar(255),
        dc_status int4 not null,
        outputData varchar(4096),
        responseData varchar(4096),
        responseRequired boolean not null,
        retrieved boolean not null,
        signature bytea,
        signed boolean not null,
        device_dc_id int4,
        operatorId int4,
        policyAppId int4,
        template_dc_id int4,
        userId int4 not null,
        primary key (dc_id)
    );

    create table as_policy (
       dc_id int4 not null,
        jsonPolicy varchar(4096),
        dc_name varchar(255),
        primary key (dc_id)
    );

    create table as_policy_app (
       dc_id int4 not null,
        authapp varchar(255),
        dc_disabled boolean,
        subId int4 not null,
        subname varchar(255),
        primary key (dc_id)
    );

    create table as_userfingerprint (
       policyAppId int4 not null,
        userId int4 not null,
        fingerprint varchar(255),
        timeStamp timestamp,
        primary key (policyAppId, userId)
    );

    create table as_version (
       dc_id int4 not null,
        clientType int4,
        dc_disabled boolean,
        downloadUrl varchar(255),
        expiresOn timestamp,
        informationUrl varchar(255),
        jpaVersion int4 not null,
        dc_name varchar(128),
        testApp boolean,
        as_version int4,
        versionStr varchar(128),
        user_dc_id int4,
        primary key (dc_id)
    );

    create table core_action (
       dc_id int4 not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
        primary key (dc_id)
    );

    create table core_group (
       dc_id int4 not null,
        jpaVersion int4 not null,
        description varchar(255),
        groupDn varchar(255),
        dc_name varchar(255) not null,
        dc_role int4,
        dc_ldap int4,
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

    create table core_ref_user_group (
       group_id int4 not null,
        user_id int4 not null
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
       dc_id int4 not null,
        active boolean not null,
        content text,
        defaultTemplate boolean not null,
        inUse boolean not null,
        jpaVersion int4 not null,
        language int4 not null,
        lastModified timestamp,
        macDigest bytea,
        dc_name varchar(128) not null,
        dc_tokens varchar(4096),
        dc_version int4,
        primary key (dc_id)
    );

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
        primary key (dc_userext_id)
    );

    create table sys_node (
       dc_id int4 not null,
        dc_name varchar(64) not null,
        startedOn timestamp,
        state int4 not null,
        wentDownOn timestamp,
        primary key (dc_id)
    );

    alter table if exists as_authApp 
       add constraint UK_AUTHAPP_NAME unique (dc_name);

    alter table if exists as_cloudsafe 
       add constraint UK_AS_CLOUDDATA unique (dc_name, owner, user_dc_id, device_dc_id, dc_parent_id, group_dc_id);
create index IDX_DEVICE_LAST_LOGIN on as_device (lastLogin, dc_state);
create index IDX_DEVICE_USER on as_device (userId);

    alter table if exists as_device 
       add constraint UK_DEVICE_USER unique (userId, name);
create index FIDO_AUTH_CREDENTIAL_ID_INDEX on as_fido_authenticator (credentialId);

    alter table if exists as_fido_authenticator 
       add constraint UK_USER_CREDENTIAL_ID unique (userId, credentialId);

    alter table if exists as_policy 
       add constraint UK_POLICY_NAME unique (dc_name);

    alter table if exists as_policy_app 
       add constraint UK_POLICY_APP unique (authapp, subId);

    alter table if exists as_version 
       add constraint UK_VERSION_NAME_TYPE unique (dc_name, versionStr, clientType);

    alter table if exists core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table if exists core_group 
       add constraint UK_APP_GROUP unique (dc_name);

    alter table if exists core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table if exists core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table if exists core_template 
       add constraint UK_APP_TEMPLATE unique (dc_name, language, dc_version);

    alter table if exists core_user 
       add constraint UK_APP_USER unique (loginId);

    alter table if exists sys_node 
       add constraint UK_NODE_NAME unique (dc_name);

    alter table if exists as_activationcode 
       add constraint FK_APP_AC_USER 
       foreign key (userId) 
       references core_user;

    alter table if exists as_app_policy_group 
       add constraint FK_REF_GROUP 
       foreign key (group_id) 
       references core_group;

    alter table if exists as_app_policy_group 
       add constraint FK_REF_APP_POLICY 
       foreign key (policyApp_id) 
       references as_policy_app;

    alter table if exists as_app_policy_group 
       add constraint FK_REF_POLICY 
       foreign key (policy_id) 
       references as_policy;

    alter table if exists as_cloudsafe 
       add constraint FK_AS_PROP_DEVICE 
       foreign key (device_dc_id) 
       references as_device;

    alter table if exists as_cloudsafe 
       add constraint FK_AS_PROP_GROUP 
       foreign key (group_dc_id) 
       references core_group;

    alter table if exists as_cloudsafe 
       add constraint FK_AS_PROP_USER_MODIFIED 
       foreign key (lastModifiedUser_dc_id) 
       references core_user;

    alter table if exists as_cloudsafe 
       add constraint FK_AS_PARENT_ID 
       foreign key (dc_parent_id) 
       references as_cloudsafe;

    alter table if exists as_cloudsafe 
       add constraint FK_AS_PROP_USER 
       foreign key (user_dc_id) 
       references core_user;

    alter table if exists as_cloudsafelimit 
       add constraint FK_CLOUDSAFE_LIMIT 
       foreign key (user_dc_id) 
       references core_user;

    alter table if exists as_cloudsafeshare 
       add constraint FK_AS_CD_SHARE 
       foreign key (cloudSafe_dc_id) 
       references as_cloudsafe 
       on delete cascade;

    alter table if exists as_cloudsafeshare 
       add constraint FK_AS_CD_GROUP 
       foreign key (group_dc_id) 
       references core_group;

    alter table if exists as_cloudsafeshare 
       add constraint FK_AS_CD_SHARE_USER 
       foreign key (user_dc_id) 
       references core_user;

    alter table if exists as_device 
       add constraint FK_APP_DEVICE_VERSION 
       foreign key (asVersion_dc_id) 
       references as_version;

    alter table if exists as_device 
       add constraint FK_APP_DEVICE_USER 
       foreign key (userId) 
       references core_user;

    alter table if exists as_fido_authenticator 
       add constraint FK_FIDO_USER 
       foreign key (userId) 
       references core_user;

    alter table if exists as_message 
       add constraint FK_APP_MSG_DEVICE 
       foreign key (device_dc_id) 
       references as_device;

    alter table if exists as_message 
       add constraint FK_MSG_OPERATOR 
       foreign key (operatorId) 
       references core_user;

    alter table if exists as_message 
       add constraint FK_MSG_POLICYAPP 
       foreign key (policyAppId) 
       references as_policy_app;

    alter table if exists as_message 
       add constraint FK_APP_MSG_TEMPLATE 
       foreign key (template_dc_id) 
       references core_template;

    alter table if exists as_message 
       add constraint FK_APP_MSG_USER 
       foreign key (userId) 
       references core_user;

    alter table if exists as_version 
       add constraint FK_APP_VERSION_USER 
       foreign key (user_dc_id) 
       references core_user;

    alter table if exists core_group 
       add constraint FK_GROUP_ROLE 
       foreign key (dc_role) 
       references core_role;

    alter table if exists core_group 
       add constraint FK_GROUP_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table if exists core_ref_user_group 
       add constraint FK_GROUP_USER 
       foreign key (user_id) 
       references core_user;

    alter table if exists core_ref_user_group 
       add constraint FK_USER_GROUP 
       foreign key (group_id) 
       references core_group;

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
