create table oauth_client (
dc_id int4 not null,
client_id varchar(255) not null,
client_secret varchar(255) not null,
dc_disabled boolean not null,
display_name varchar(255) not null,
dc_metadata text,
redirect_uris varchar(255),
primary key (dc_id)
);

create table oauth_token (
client_id int4 not null,
user_id int4 not null,
access_token varchar(255),
at_expires_on timestamp,
claims_request varchar(255),
last_authenticated timestamp,
refresh_token varchar(255),
rt_expires_on timestamp,
scope varchar(255),
primary key (client_id, user_id)
);

alter table oauth_client
add constraint UK_OAUTH_CLIENT_ENTITYID unique (client_id);

alter table oauth_client
add constraint UK_OAUTH_CLIENT_DISPLAY_NAME unique (display_name);
