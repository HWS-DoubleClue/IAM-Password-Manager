
    create table core_seq (
       seq_name varchar2(255 char) not null,
        seq_value number(19,0),
        primary key (seq_name)
    );

    insert into core_seq(seq_name, seq_value) values ('OAUTH_CLIENT.ID',1);

    create table oauth_client (
       dc_id number(10,0) not null,
        client_id varchar2(255 char) not null,
        client_secret varchar2(255 char) not null,
        dc_disabled number(1,0) not null,
        display_name varchar2(255 char) not null,
        idp_settings long,
        dc_metadata clob,
        redirect_uris varchar2(255 char),
        primary key (dc_id)
    );

    create table oauth_token (
       client_id number(10,0) not null,
        user_id number(10,0) not null,
        access_token varchar2(255 char),
        at_expires_on timestamp,
        claims_request varchar2(255 char),
        last_authenticated timestamp,
        refresh_token varchar2(255 char),
        rt_expires_on timestamp,
        scope varchar2(255 char),
        primary key (client_id, user_id)
    );

    alter table oauth_client 
       add constraint UK_OAUTH_CLIENT_ENTITYID unique (client_id);

    alter table oauth_client 
       add constraint UK_OAUTH_CLIENT_DISPLAY_NAME unique (display_name);
