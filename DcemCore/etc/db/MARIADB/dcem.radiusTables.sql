
    create table core_seq (
       seq_name varchar(255) not null,
        seq_value bigint,
        primary key (seq_name)
    ) engine=InnoDB;

    insert into core_seq(seq_name, seq_value) values ('RADIUS_CLIENT.ID',1);

    insert into core_seq(seq_name, seq_value) values ('RADIUS_REP.ID',1);

    create table radius_client (
       dc_id integer not null,
        ignoreUsersPassword bit not null,
        ipNumber varchar(255) not null,
        name varchar(255) not null,
        settingsJson varchar(4096),
        sharedSecret mediumblob not null,
        useChallenge bit not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table radius_report (
       dc_id integer not null,
        action integer,
        details varchar(1024),
        error bit not null,
        nasClientName varchar(128),
        dc_time datetime not null,
        primary key (dc_id)
    ) engine=InnoDB;

    alter table radius_client 
       add constraint UK_RADIUS_IPNUMBER unique (ipNumber);
