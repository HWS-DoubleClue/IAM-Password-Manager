
    create table core_action (
       dc_id integer not null,
        action varchar(128) not null,
        moduleId varchar(64) not null,
        subject varchar(128) not null,
        primary key (dc_id)
    );

    create table core_group (
       dc_id integer not null,
        jpaVersion integer not null,
        description varchar(255),
        groupDn varchar(255),
        dc_name varchar(255) not null,
        dc_role integer,
        dc_ldap integer,
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

    create table core_ref_user_group (
       group_id integer not null,
        user_id integer not null
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
       dc_id integer not null,
        acSuspendedTill timestamp,
        disabled boolean not null,
        displayName varchar(128),
        email varchar(255),
        failActivations integer not null,
        hashPassword long varchar for bit data,
        hmac varchar(32) for bit data not null,
        jpaVersion integer not null,
        locale integer,
        lastLogin timestamp,
        loginId varchar(64) not null,
        mobileNumber varchar(255),
        objectGuid varchar(255) for bit data,
        passCounter integer not null,
        prvMobile varchar(32),
        dc_salt varchar(32) for bit data,
        saveit long varchar for bit data,
        dc_tel varchar(255),
        userDn varchar(255),
        userPrincipalName varchar(128),
        dc_role integer not null,
        dc_ldap integer,
        primary key (dc_id)
    );

    create table shifts_absence (
       dc_id integer not null,
        absenseType integer,
        comment varchar(255),
        currentDate date,
        endDate date,
        startDate date,
        dc_user integer not null,
        primary key (dc_id)
    );

    create table shifts_assignments (
       dc_id integer not null,
        duration float,
        endDate time,
        startDate time,
        shifts_user_id integer,
        shifts_shift_id integer,
        primary key (dc_id)
    );

    create table shifts_entry (
       dc_id integer not null,
        endDate date,
        resourcesRequired integer not null,
        startDate date,
        dc_team integer not null,
        dc_type integer not null,
        primary key (dc_id)
    );

    create table shifts_shift (
       dc_id integer not null,
        comment varchar(255),
        ignoreWarning boolean,
        shiftDate date,
        dc_shiftentry integer not null,
        primary key (dc_id)
    );

    create table shifts_ShiftUsers_Types (
       shiftsuser_id integer not null,
        shiftstype_id integer not null,
        primary key (shiftsuser_id, shiftstype_id)
    );

    create table shifts_skills (
       dc_id integer not null,
        skillAbbriviation varchar(255),
        skillName varchar(255),
        primary key (dc_id)
    );

    create table shifts_team (
       dc_id integer not null,
        name varchar(255),
        dc_style varchar(255),
        primary key (dc_id)
    );

    create table shifts_type (
       dc_id integer not null,
        colorCode varchar(255),
        duration float,
        endTime time,
        isOnCall boolean,
        dc_name varchar(255),
        startTime time,
        dc_style varchar(255),
        primary key (dc_id)
    );

    create table shifts_type_days (
       ShiftsTypeEntity_dc_id integer not null,
        workingDays varchar(255)
    );

    create table shifts_user (
       dc_id integer not null,
        availableOn date,
        exitDate date,
        dc_external boolean not null,
        onCallAllowed boolean not null,
        onCallNumber varchar(255),
        dc_user integer not null,
        primary key (dc_id)
    );

    create table shifts_users_skills (
       shiftsuser_id integer not null,
        shiftsskills_id integer not null,
        primary key (shiftsuser_id, shiftsskills_id)
    );
create unique index UK_SEM_ACTION on core_action (moduleId, subject, action);
create unique index UK_APP_GROUP on core_group (dc_name);
create unique index UK_LDAP_NAME on core_ldap (name);
create unique index UK_ROLE_NAME on core_role (dc_name);
create unique index UK_APP_USER on core_user (loginId);
create unique index UK_SKILL_ABBR_UNIQUE on shifts_skills (skillAbbriviation);
create unique index UK_SKILL_NAME_UNIQUE on shifts_skills (skillName);

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
       add constraint FK_USER_LDAP 
       foreign key (dc_ldap) 
       references core_ldap;

    alter table shifts_absence 
       add constraint FK_SHIFTS_ABSENCE_USER 
       foreign key (dc_user) 
       references shifts_user;

    alter table shifts_assignments 
       add constraint FKioty4hgetv6s1kr17rf0sa33u 
       foreign key (shifts_user_id) 
       references shifts_user;

    alter table shifts_assignments 
       add constraint FK1kg14y068bxsqfehje32vqx9y 
       foreign key (shifts_shift_id) 
       references shifts_shift;

    alter table shifts_entry 
       add constraint FK_SHIFTS_TEAM 
       foreign key (dc_team) 
       references shifts_team;

    alter table shifts_entry 
       add constraint FK_SHIFTS_TYPE 
       foreign key (dc_type) 
       references shifts_type;

    alter table shifts_shift 
       add constraint FK_SHIFTS_ENTRY_SHIFT 
       foreign key (dc_shiftentry) 
       references shifts_entry;

    alter table shifts_ShiftUsers_Types 
       add constraint FK6oxqea21h6fulncfp55k59nl2 
       foreign key (shiftstype_id) 
       references shifts_type;

    alter table shifts_ShiftUsers_Types 
       add constraint FKm7tuhl6stntbyv10ed887nl1l 
       foreign key (shiftsuser_id) 
       references shifts_user;

    alter table shifts_type_days 
       add constraint FKb1keg8dwmv5i1dmo4hc5gxl4x 
       foreign key (ShiftsTypeEntity_dc_id) 
       references shifts_type;

    alter table shifts_user 
       add constraint FK_SHIFTS_USER 
       foreign key (dc_user) 
       references core_user;

    alter table shifts_users_skills 
       add constraint FK54uax9oju7j5l5a76qs607s4a 
       foreign key (shiftsskills_id) 
       references shifts_skills;

    alter table shifts_users_skills 
       add constraint FKn3704yqfl7rghxok589294ejj 
       foreign key (shiftsuser_id) 
       references shifts_user;
