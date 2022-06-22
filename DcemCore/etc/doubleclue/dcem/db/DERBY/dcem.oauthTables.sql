create table oauth_client (
dc_id integer not null,
client_id varchar(255) not null,
client_secret varchar(255) not null,
dc_disabled boolean not null,
display_name varchar(255) not null,
dc_metadata clob(10M),
redirect_uris varchar(255),
primary key (dc_id)
);

create table oauth_token (
client_id integer not null,
user_id integer not null,
access_token varchar(255),
at_expires_on timestamp,
claims_request varchar(255),
last_authenticated timestamp,
refresh_token varchar(255),
rt_expires_on timestamp,
scope varchar(255),
primary key (client_id, user_id)
);
create unique index UK_OAUTH_CLIENT_ENTITYID on oauth_client (client_id);
create unique index UK_OAUTH_CLIENT_DISPLAY_NAME on oauth_client (display_name);
