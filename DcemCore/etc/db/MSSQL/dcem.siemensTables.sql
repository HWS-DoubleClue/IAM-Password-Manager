
    create table siemens_licenserequest (
       dc_id int not null,
        approved_by varchar(255),
        expiration_date datetime2,
        further_info varchar(255),
        last_modified datetime2 not null,
        author varchar(255) not null,
        license_type int not null,
        mac_dongle varchar(255) not null,
        machine_type int not null,
        requested_date datetime2 not null,
        serial_nr varchar(255) not null,
        service_key_level int not null,
        dc_status int not null,
        subject varchar(255) not null,
        sw_version varchar(255) not null,
        system varchar(255) not null,
        terms_conditions varchar(255),
        primary key (dc_id)
    );

    create table siemens_licensetypes (
       dc_id int not null,
        dc_description varchar(255),
        dc_name varchar(255) not null,
        primary key (dc_id)
    );

    create table siemens_licensetypesseq (
       next_val bigint
    );

    insert into siemens_licensetypesseq values ( 1 );

    create table siemens_machinetypes (
       dc_id int not null,
        dc_description varchar(255),
        dc_name varchar(255) not null,
        primary key (dc_id)
    );

    create table siemens_machinetypeseq (
       next_val bigint
    );

    insert into siemens_machinetypeseq values ( 1 );

    create table siemens_requestseq (
       next_val bigint
    );

    insert into siemens_requestseq values ( 1 );
