
    create table core_seq (
       seq_name varchar(255) not null,
        seq_value bigint,
        primary key (seq_name)
    );

    insert into core_seq(seq_name, seq_value) values ('SAML_SP_METADATA.ID',1);

    create table saml_sp_metadata (
       dc_id integer not null,
        acs_location varchar(255) not null,
        certificateString clob(255),
        dc_disabled boolean not null,
        display_name varchar(255) not null,
        entityId varchar(255) not null,
        idp_settings varchar(4096),
        logout_is_post boolean not null,
        logout_location varchar(255),
        dc_metadata clob(255),
        name_id_format integer not null,
        requests_signed boolean not null,
        primary key (dc_id)
    );
create unique index UK_SP_METADATA_ENTITYID on saml_sp_metadata (entityId);
create unique index UK_SP_METADATA_DISPLAY_NAME on saml_sp_metadata (display_name);
