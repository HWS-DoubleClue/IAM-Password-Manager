
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

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_absence.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_assignments.ID',1);

    insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_type.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_team.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_entry.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_shift.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_skills.ID',1);

    create table core_user (
       dc_id int4 not null,
        acSuspendedTill timestamp,
        disabled boolean not null,
        displayName varchar(128),
        email varchar(255),
        failActivations int4 not null,
        hashPassword bytea,
        hmac bytea not null,
        jpaVersion int4 not null,
        locale int4,
        lastLogin timestamp,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        objectGuid bytea,
        passCounter int4 not null,
        prvMobile varchar(32),
        dc_salt bytea,
        saveit bytea,
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(128),
        dc_role int4 not null,
        dc_ldap int4,
        primary key (dc_id)
    );

    create table shifts_absence (
       dc_id int4 not null,
        absenseType int4,
        comment varchar(255),
        currentDate date,
        endDate date,
        startDate date,
        dc_user int4 not null,
        primary key (dc_id)
    );

    create table shifts_assignments (
       dc_id int4 not null,
        duration float4,
        endDate time,
        startDate time,
        shifts_user_id int4,
        shifts_shift_id int4,
        primary key (dc_id)
    );

    create table shifts_entry (
       dc_id int4 not null,
        endDate date,
        resourcesRequired int4 not null,
        startDate date,
        dc_team int4 not null,
        dc_type int4 not null,
        primary key (dc_id)
    );

    create table shifts_shift (
       dc_id int4 not null,
        comment varchar(255),
        ignoreWarning boolean,
        shiftDate date,
        dc_shiftentry int4 not null,
        primary key (dc_id)
    );

    create table shifts_ShiftUsers_Types (
       shiftsuser_id int4 not null,
        shiftstype_id int4 not null,
        primary key (shiftsuser_id, shiftstype_id)
    );

    create table shifts_skills (
       dc_id int4 not null,
        skillAbbriviation varchar(255),
        skillName varchar(255),
        primary key (dc_id)
    );

    create table shifts_team (
       dc_id int4 not null,
        name varchar(255),
        dc_style varchar(255),
        primary key (dc_id)
    );

    create table shifts_type (
       dc_id int4 not null,
        colorCode varchar(255),
        duration float4,
        endTime time,
        isOnCall boolean,
        dc_name varchar(255),
        startTime time,
        dc_style varchar(255),
        primary key (dc_id)
    );

    create table shifts_type_days (
       ShiftsTypeEntity_dc_id int4 not null,
        workingDays varchar(255)
    );

    create table shifts_user (
       dc_id int4 not null,
        availableOn date,
        exitDate date,
        dc_external boolean not null,
        onCallAllowed boolean not null,
        onCallNumber varchar(255),
        dc_user int4 not null,
        primary key (dc_id)
    );

    create table shifts_users_skills (
       shiftsuser_id int4 not null,
        shiftsskills_id int4 not null,
        primary key (shiftsuser_id, shiftsskills_id)
    );

    alter table if exists core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table if exists core_group 
       add constraint UK_APP_GROUP unique (dc_name);

    alter table if exists core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table if exists core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table if exists core_user 
       add constraint UK_APP_USER unique (loginId);

    alter table if exists shifts_skills 
       add constraint UK_SKILL_ABBR_UNIQUE unique (skillAbbriviation);

    alter table if exists shifts_skills 
       add constraint UK_SKILL_NAME_UNIQUE unique (skillName);

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
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table if exists shifts_absence 
       add constraint FK_SHIFTS_ABSENCE_USER 
       foreign key (dc_user) 
       references shifts_user;

    alter table if exists shifts_assignments 
       add constraint FKioty4hgetv6s1kr17rf0sa33u 
       foreign key (shifts_user_id) 
       references shifts_user;

    alter table if exists shifts_assignments 
       add constraint FK1kg14y068bxsqfehje32vqx9y 
       foreign key (shifts_shift_id) 
       references shifts_shift;

    alter table if exists shifts_entry 
       add constraint FK_SHIFTS_TEAM 
       foreign key (dc_team) 
       references shifts_team;

    alter table if exists shifts_entry 
       add constraint FK_SHIFTS_TYPE 
       foreign key (dc_type) 
       references shifts_type;

    alter table if exists shifts_shift 
       add constraint FK_SHIFTS_ENTRY_SHIFT 
       foreign key (dc_shiftentry) 
       references shifts_entry;

    alter table if exists shifts_ShiftUsers_Types 
       add constraint FK6oxqea21h6fulncfp55k59nl2 
       foreign key (shiftstype_id) 
       references shifts_type;

    alter table if exists shifts_ShiftUsers_Types 
       add constraint FKm7tuhl6stntbyv10ed887nl1l 
       foreign key (shiftsuser_id) 
       references shifts_user;

    alter table if exists shifts_type_days 
       add constraint FKb1keg8dwmv5i1dmo4hc5gxl4x 
       foreign key (ShiftsTypeEntity_dc_id) 
       references shifts_type;

    alter table if exists shifts_user 
       add constraint FK_SHIFTS_USER 
       foreign key (dc_user) 
       references core_user;

    alter table if exists shifts_users_skills 
       add constraint FK54uax9oju7j5l5a76qs607s4a 
       foreign key (shiftsskills_id) 
       references shifts_skills;

    alter table if exists shifts_users_skills 
       add constraint FKn3704yqfl7rghxok589294ejj 
       foreign key (shiftsuser_id) 
       references shifts_user;
