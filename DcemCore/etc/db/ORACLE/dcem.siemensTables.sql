create sequence siemens_licensetypesseq start with 1 increment by  4;
create sequence siemens_machinetypeseq start with 1 increment by  4;
create sequence siemens_requestseq start with 1 increment by  4;

    create table siemens_licenserequest (
       dc_id number(10,0) not null,
        approved_by varchar2(255 char),
        expiration_date timestamp,
        further_info varchar2(255 char),
        last_modified timestamp not null,
        author varchar2(255 char) not null,
        license_type number(10,0) not null,
        mac_dongle varchar2(255 char) not null,
        machine_type number(10,0) not null,
        requested_date timestamp not null,
        serial_nr varchar2(255 char) not null,
        service_key_level number(10,0) not null,
        dc_status number(10,0) not null,
        subject varchar2(255 char) not null,
        sw_version varchar2(255 char) not null,
        system varchar2(255 char) not null,
        terms_conditions varchar2(255 char),
        primary key (dc_id)
    );

    create table siemens_licensetypes (
       dc_id number(10,0) not null,
        dc_description varchar2(255 char),
        dc_name varchar2(255 char) not null,
        primary key (dc_id)
    );

    create table siemens_machinetypes (
       dc_id number(10,0) not null,
        dc_description varchar2(255 char),
        dc_name varchar2(255 char) not null,
        primary key (dc_id)
    );
