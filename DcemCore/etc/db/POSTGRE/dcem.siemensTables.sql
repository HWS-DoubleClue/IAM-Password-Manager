create sequence siemens_licensetypesseq start 1 increment 4;
create sequence siemens_machinetypeseq start 1 increment 4;
create sequence siemens_requestseq start 1 increment 4;

    create table siemens_licenserequest (
       dc_id int4 not null,
        approved_by varchar(255),
        expiration_date timestamp,
        further_info varchar(255),
        last_modified timestamp not null,
        author varchar(255) not null,
        license_type int4 not null,
        mac_dongle varchar(255) not null,
        machine_type int4 not null,
        requested_date timestamp not null,
        serial_nr varchar(255) not null,
        service_key_level int4 not null,
        dc_status int4 not null,
        subject varchar(255) not null,
        sw_version varchar(255) not null,
        system varchar(255) not null,
        terms_conditions varchar(255),
        primary key (dc_id)
    );

    create table siemens_licensetypes (
       dc_id int4 not null,
        dc_description varchar(255),
        dc_name varchar(255) not null,
        primary key (dc_id)
    );

    create table siemens_machinetypes (
       dc_id int4 not null,
        dc_description varchar(255),
        dc_name varchar(255) not null,
        primary key (dc_id)
    );
