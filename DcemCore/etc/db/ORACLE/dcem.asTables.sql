
    create table as_activationcode (
       dc_id number(10,0) not null,
        activationCode long raw not null,
        createdOn timestamp not null,
        info varchar2(255 char),
        validTill timestamp not null,
        userId number(10,0) not null,
        primary key (dc_id)
    );

    create table as_app_policy_group (
       dc_id number(10,0) not null,
        dc_priority number(10,0),
        group_id number(10,0),
        policyApp_id number(10,0) not null,
        policy_id number(10,0),
        primary key (dc_id)
    );

    create table as_authApp (
       dc_id number(10,0) not null,
        disabled number(1,0) not null,
        dc_name varchar2(64 char),
        retryCounter number(10,0) not null,
        sharedKey long raw,
        primary key (dc_id)
    );

    create table as_cloudsafe (
       dc_id number(10,0) not null,
        discardAfter timestamp,
        dc_is_folder number(1,0) not null,
        dc_gcm number(1,0) not null,
        lastModified timestamp,
        dc_length number(19,0),
        dc_name varchar2(255 char) not null,
        options varchar2(255 char),
        owner number(10,0),
        recycled number(1,0) not null,
        dc_salt raw(32),
        device_dc_id number(10,0) not null,
        group_dc_id number(10,0),
        lastModifiedUser_dc_id number(10,0),
        dc_parent_id number(10,0),
        user_dc_id number(10,0) not null,
        primary key (dc_id)
    );

    create table as_cloudsafecontent (
       cloudDataEntity_dc_id number(10,0) not null,
        content blob,
        primary key (cloudDataEntity_dc_id)
    );

    create table as_cloudsafelimit (
       expiry_date timestamp,
        dc_limit number(19,0) not null,
        ps_enabled number(1,0) not null,
        dc_used number(19,0) not null,
        user_dc_id number(10,0) not null,
        primary key (user_dc_id)
    );

    create table as_cloudsafeshare (
       dc_id number(10,0) not null,
        restrictDownload number(1,0),
        writeAccess number(1,0),
        cloudSafe_dc_id number(10,0),
        group_dc_id number(10,0),
        user_dc_id number(10,0),
        primary key (dc_id)
    );

    create table as_device (
       dc_id number(10,0) not null,
        appOsVersion varchar2(255 char),
        deviceHash raw(255),
        deviceKey long raw,
        lastLogin timestamp,
        locale varchar2(2 char),
        manufacture varchar2(255 char),
        name varchar2(64 char),
        nodeId number(10,0),
        offlineCounter number(10,0) not null,
        offlineKey long raw,
        publicKey raw(1024),
        retryCounter number(10,0) not null,
        risks varchar2(255 char),
        dc_state number(10,0) not null,
        dc_status number(10,0) not null,
        udid raw(255),
        asVersion_dc_id number(10,0),
        userId number(10,0) not null,
        primary key (dc_id)
    );

    create table as_fido_authenticator (
       dc_id number(10,0) not null,
        credentialId varchar2(255 char) not null,
        display_name varchar2(255 char) not null,
        lastUsed timestamp not null,
        passwordless number(1,0) not null,
        publicKey raw(1024) not null,
        registeredOn timestamp not null,
        userId number(10,0) not null,
        primary key (dc_id)
    );

    create table as_message (
       dc_id number(19,0) not null,
        actionId varchar2(64 char),
        createdOn timestamp not null,
        info varchar2(255 char),
        dc_status number(10,0) not null,
        outputData long,
        responseData long,
        responseRequired number(1,0) not null,
        retrieved number(1,0) not null,
        signature raw(255),
        signed number(1,0) not null,
        device_dc_id number(10,0),
        operatorId number(10,0),
        policyAppId number(10,0),
        template_dc_id number(10,0),
        userId number(10,0) not null,
        primary key (dc_id)
    );

    create table as_policy (
       dc_id number(10,0) not null,
        jsonPolicy long,
        dc_name varchar2(255 char),
        primary key (dc_id)
    );

    create table as_policy_app (
       dc_id number(10,0) not null,
        authapp varchar2(255 char),
        dc_disabled number(1,0),
        subId number(10,0) not null,
        subname varchar2(255 char),
        primary key (dc_id)
    );

    create table as_userfingerprint (
       policyAppId number(10,0) not null,
        userId number(10,0) not null,
        fingerprint varchar2(255 char),
        timeStamp timestamp,
        primary key (policyAppId, userId)
    );

    create table as_version (
       dc_id number(10,0) not null,
        clientType number(10,0),
        dc_disabled number(1,0),
        downloadUrl varchar2(255 char),
        expiresOn timestamp,
        informationUrl varchar2(255 char),
        jpaVersion number(10,0) not null,
        dc_name varchar2(128 char),
        testApp number(1,0),
        as_version number(10,0),
        versionStr varchar2(128 char),
        user_dc_id number(10,0),
        primary key (dc_id)
    );

    create table core_action (
       dc_id number(10,0) not null,
        action varchar2(128 char) not null,
        moduleId varchar2(64 char) not null,
        subject varchar2(128 char) not null,
        primary key (dc_id)
    );

    create table core_group (
       dc_id number(10,0) not null,
        jpaVersion number(10,0) not null,
        description varchar2(255 char),
        groupDn varchar2(255 char),
        dc_name varchar2(255 char) not null,
        dc_role number(10,0),
        dc_ldap number(10,0),
        primary key (dc_id)
    );

    create table core_ldap (
       dc_id number(10,0) not null,
        baseDN varchar2(255 char) not null,
        configJson long,
        domainType number(10,0) not null,
        enable number(1,0) not null,
        filter varchar2(255 char) not null,
        firstNameAttribute varchar2(255 char) not null,
        host varchar2(255 char) not null,
        lastNameAttribute varchar2(255 char) not null,
        loginAttribute varchar2(255 char) not null,
        mailAttribute varchar2(255 char),
        mapEmailDomains varchar2(255 char),
        mobileAttribute varchar2(255 char),
        name varchar2(64 char) not null,
        password long raw not null,
        dc_rank number(10,0),
        searchAccount varchar2(255 char) not null,
        telephoneAttribute varchar2(255 char),
        dc_version number(10,0),
        primary key (dc_id)
    );

    create table core_ref_user_group (
       group_id number(10,0) not null,
        user_id number(10,0) not null
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
       dc_id number(10,0) not null,
        active number(1,0) not null,
        content clob,
        defaultTemplate number(1,0) not null,
        inUse number(1,0) not null,
        jpaVersion number(10,0) not null,
        language number(10,0) not null,
        lastModified timestamp,
        macDigest raw(32),
        dc_name varchar2(128 char) not null,
        dc_tokens long,
        dc_version number(10,0),
        primary key (dc_id)
    );

    create table core_user (
       dc_id number(10,0) not null,
        acSuspendedTill timestamp,
        disabled number(1,0) not null,
        displayName varchar2(255 char),
        email varchar2(255 char),
        failActivations number(10,0) not null,
        hashPassword long raw,
        hmac raw(32) not null,
        jpaVersion number(10,0) not null,
        locale number(10,0),
        lastLogin timestamp,
        loginId varchar2(255 char) not null,
        mobileNumber varchar2(255 char),
        objectGuid raw(255),
        passCounter number(10,0) not null,
        privateEmail varchar2(255 char),
        prvMobile varchar2(32 char),
        dc_salt raw(32),
        saveit long raw,
        dc_tel varchar2(255 char),
        userDn varchar2(255 char),
        userPrincipalName varchar2(255 char),
        dc_role number(10,0) not null,
        userext number(10,0),
        dc_ldap number(10,0),
        primary key (dc_id)
    );

    create table core_userext (
       dc_userext_id number(10,0) not null,
        dc_country varchar2(255 char),
        photo long raw,
        dc_timezone varchar2(255 char),
        primary key (dc_userext_id)
    );

    create table sys_node (
       dc_id number(10,0) not null,
        dc_name varchar2(64 char) not null,
        startedOn timestamp,
        state number(10,0) not null,
        wentDownOn timestamp,
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
