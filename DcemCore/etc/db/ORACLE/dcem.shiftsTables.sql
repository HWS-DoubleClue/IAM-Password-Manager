
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

    insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_absence.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_assignments.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_roster_user.ID',1);

    insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_type.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_roster.ID',1);

    insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_roster_shift.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_team.ID',1);

    insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_entry.ID',1);

    insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_shift.ID',1);

    insert into core_seq(seq_name, seq_value) values ('shifts_skills.ID',1);

    create table core_user (
       dc_id number(10,0) not null,
        acSuspendedTill timestamp,
        disabled number(1,0) not null,
        displayName varchar2(255 char),
        email varchar2(255 char),
        failActivations number(10,0) not null,
        hashPassword long raw,
        hmac blob not null,
        jpaVersion number(10,0) not null,
        locale number(10,0),
        lastLogin timestamp,
        loginId varchar2(255 char) not null,
        mobileNumber varchar2(255 char),
        objectGuid blob,
        passCounter number(10,0) not null,
        privateEmail varchar2(255 char),
        prvMobile varchar2(32 char),
        dc_salt blob,
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
        photo blob,
        dc_timezone varchar2(255 char),
        primary key (dc_userext_id)
    );

    create table shifts_absence (
       dc_id number(10,0) not null,
        absenseType number(10,0),
        comment varchar2(255 char),
        currentDate date,
        endDate date,
        startDate date,
        dc_user number(10,0) not null,
        primary key (dc_id)
    );

    create table shifts_assignments (
       dc_id number(10,0) not null,
        duration float,
        endDate date,
        startDate date,
        shifts_user_id number(10,0),
        shifts_shift_id number(10,0),
        primary key (dc_id)
    );

    create table shifts_entry (
       dc_id number(10,0) not null,
        endDate date,
        resourcesRequired number(10,0) not null,
        startDate date,
        dc_team number(10,0) not null,
        dc_type number(10,0) not null,
        primary key (dc_id)
    );

    create table shifts_roster (
       dc_id number(10,0) not null,
        endDate timestamp,
        rosterName varchar2(255 char),
        rosterType number(10,0),
        skipWeekends number(1,0) not null,
        startDate timestamp,
        primary key (dc_id)
    );

    create table shifts_roster_shift (
       dc_id number(10,0) not null,
        numbering number(10,0) not null,
        resources number(10,0) not null,
        roster_shift number(10,0) not null,
        dc_team number(10,0) not null,
        dc_type number(10,0) not null,
        primary key (dc_id)
    );

    create table shifts_roster_shifts_roster_shift (
       ShiftsRosterEntity_dc_id number(10,0) not null,
        shiftsRosterTypeEntity_dc_id number(10,0) not null
    );

    create table shifts_roster_user (
       dc_id number(10,0) not null,
        dc_offset number(10,0),
        dc_user number(10,0) not null,
        shifts_roster number(10,0) not null,
        primary key (dc_id)
    );

    create table shifts_shift (
       dc_id number(10,0) not null,
        comment varchar2(255 char),
        ignoreWarning number(1,0),
        shiftDate date,
        dc_shiftentry number(10,0) not null,
        primary key (dc_id)
    );

    create table shifts_ShiftUsers_Types (
       shiftsuser_id number(10,0) not null,
        shiftstype_id number(10,0) not null,
        primary key (shiftsuser_id, shiftstype_id)
    );

    create table shifts_skills (
       dc_id number(10,0) not null,
        skillAbbriviation varchar2(255 char),
        skillName varchar2(255 char),
        primary key (dc_id)
    );

    create table shifts_team (
       dc_id number(10,0) not null,
        name varchar2(255 char),
        dc_style varchar2(255 char),
        primary key (dc_id)
    );

    create table shifts_type (
       dc_id number(10,0) not null,
        colorCode varchar2(255 char),
        duration float,
        endTime date,
        isOnCall number(1,0),
        dc_name varchar2(255 char),
        startTime date,
        dc_style varchar2(255 char),
        primary key (dc_id)
    );

    create table shifts_type_days (
       ShiftsTypeEntity_dc_id number(10,0) not null,
        workingDays varchar2(255 char)
    );

    create table shifts_user (
       dc_id number(10,0) not null,
        availableOn date,
        exitDate date,
        dc_external number(1,0) not null,
        federalState varchar2(255 char),
        onCallAllowed number(1,0) not null,
        onCallNumber varchar2(255 char),
        userSettings long,
        dc_user number(10,0) not null,
        dc_team number(10,0),
        primary key (dc_id)
    );

    create table shifts_users_skills (
       shiftsuser_id number(10,0) not null,
        shiftsskills_id number(10,0) not null,
        primary key (shiftsuser_id, shiftsskills_id)
    );

    alter table core_action 
       add constraint UK_SEM_ACTION unique (moduleId, subject, action);

    alter table core_group 
       add constraint UK_APP_GROUP unique (dc_name);

    alter table core_ldap 
       add constraint UK_LDAP_NAME unique (name);

    alter table core_role 
       add constraint UK_ROLE_NAME unique (dc_name);

    alter table core_user 
       add constraint UK_APP_USER unique (loginId);

    alter table shifts_skills 
       add constraint UK_SKILL_ABBR_UNIQUE unique (skillAbbriviation);

    alter table shifts_skills 
       add constraint UK_SKILL_NAME_UNIQUE unique (skillName);

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

    alter table shifts_absence 
       add constraint FK_SHIFTS_ABSENCE_USER 
       foreign key (dc_user) 
       references shifts_user;

    alter table shifts_assignments 
       add constraint FK_SHIFTS_ID_USER 
       foreign key (shifts_user_id) 
       references shifts_user;

    alter table shifts_assignments 
       add constraint FK_SHIFTS_ID_SHIFTS 
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

    alter table shifts_roster_shift 
       add constraint FK_SHIFTS_ROSTER_SHIFT 
       foreign key (roster_shift) 
       references shifts_roster;

    alter table shifts_roster_shift 
       add constraint FK_SHIFTS_TEAM_ROSTER 
       foreign key (dc_team) 
       references shifts_team;

    alter table shifts_roster_shift 
       add constraint FK_SHIFTS_TYPE_ROSTER 
       foreign key (dc_type) 
       references shifts_type;

    alter table shifts_roster_shifts_roster_shift 
       add constraint FK9enfuq6l2blh36xm24vxse7yr 
       foreign key (shiftsRosterTypeEntity_dc_id) 
       references shifts_roster_shift;

    alter table shifts_roster_shifts_roster_shift 
       add constraint FK_SHIFTS_ROSTER_TYPE 
       foreign key (ShiftsRosterEntity_dc_id) 
       references shifts_roster;

    alter table shifts_roster_user 
       add constraint FK_SHIFTS_ROSTER_USER 
       foreign key (dc_user) 
       references shifts_user;

    alter table shifts_roster_user 
       add constraint FK_SHIFTS_ROSTER 
       foreign key (shifts_roster) 
       references shifts_roster;

    alter table shifts_shift 
       add constraint FK_SHIFTS_ENTRY_SHIFT 
       foreign key (dc_shiftentry) 
       references shifts_entry;

    alter table shifts_ShiftUsers_Types 
       add constraint FK_SHIFTS_TYPE_USERS 
       foreign key (shiftstype_id) 
       references shifts_type;

    alter table shifts_ShiftUsers_Types 
       add constraint FKm7tuhl6stntbyv10ed887nl1l 
       foreign key (shiftsuser_id) 
       references shifts_user;

    alter table shifts_type_days 
       add constraint FK_SHIFTS_TYPE_DAYS 
       foreign key (ShiftsTypeEntity_dc_id) 
       references shifts_type;

    alter table shifts_user 
       add constraint FK_SHIFTS_USER 
       foreign key (dc_user) 
       references core_user;

    alter table shifts_user 
       add constraint FK_USER_TEAM 
       foreign key (dc_team) 
       references shifts_team;

    alter table shifts_users_skills 
       add constraint FK_SHIFTS_SKILL_SKILLS 
       foreign key (shiftsskills_id) 
       references shifts_skills;

    alter table shifts_users_skills 
       add constraint FK_SHIFTS_SKILL_SKILLS 
       foreign key (shiftsuser_id) 
       references shifts_user;
