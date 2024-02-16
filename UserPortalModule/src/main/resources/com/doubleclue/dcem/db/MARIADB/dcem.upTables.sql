
create table up_apphubseq (
next_val bigint
) engine=InnoDB;

insert into up_apphubseq values ( 1 );

create table up_applicationhub (
up_id integer not null,
application varchar(10000) not null,
logo blob,
up_name varchar(255) not null,
primary key (up_id)
) engine=InnoDB;

create table up_keepassentry (
dc_id varchar(255) not null,
application varchar(10000),
up_name varchar(255) not null,
appEntity integer,
primary key (dc_id)
) engine=InnoDB;

alter table up_applicationhub
add constraint UK_APPHUB_NAME unique (up_name);

alter table up_keepassentry
add constraint FK_KEEPASS_APP
foreign key (appEntity)
references up_applicationhub (up_id);
