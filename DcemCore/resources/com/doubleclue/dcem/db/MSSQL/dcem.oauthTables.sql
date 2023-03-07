
create table oauth_client (
dc_id int not null,
client_id varchar(255) not null,
client_secret varchar(255) not null,
dc_disabled bit not null,
display_name varchar(255) not null,
idp_settings varchar(4096),
dc_metadata varchar(MAX),
redirect_uris varchar(255),
primary key (dc_id)
);

create table oauth_token (
client_id int not null,
user_id int not null,
access_token varchar(255),
at_expires_on datetime2,
claims_request varchar(255),
last_authenticated datetime2,
refresh_token varchar(255),
rt_expires_on datetime2,
scope varchar(255),
primary key (client_id, user_id)
);

alter table oauth_client
add constraint UK_OAUTH_CLIENT_ENTITYID unique (client_id);

alter table oauth_client
add constraint UK_OAUTH_CLIENT_DISPLAY_NAME unique (display_name);
