
create table oauth_client (
dc_id integer not null,
client_id varchar(255) not null,
client_secret varchar(255) not null,
dc_disabled bit not null,
display_name varchar(255) not null,
idp_settings varchar(4096),
dc_metadata longtext,
redirect_uris varchar(255),
primary key (dc_id)
) engine=InnoDB;

create table oauth_token (
client_id integer not null,
user_id integer not null,
access_token varchar(255),
at_expires_on datetime,
claims_request varchar(255),
last_authenticated datetime,
refresh_token varchar(255),
rt_expires_on datetime,
scope varchar(255),
primary key (client_id, user_id)
) engine=InnoDB;

alter table oauth_client
add constraint UK_OAUTH_CLIENT_ENTITYID unique (client_id);

alter table oauth_client
add constraint UK_OAUTH_CLIENT_DISPLAY_NAME unique (display_name);
