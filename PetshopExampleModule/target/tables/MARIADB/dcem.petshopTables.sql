
    create table petshop_orderseq (
       next_val bigint
    ) engine=InnoDB;

    insert into petshop_orderseq values ( 1 );

    create table petshop_pet (
       dc_id integer not null,
        dc_age integer not null,
        dc_name varchar(128) not null,
        petType integer not null,
        dc_price integer not null,
        dc_reserved bit not null,
        dc_sex integer not null,
        dc_sold bit not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table petshop_petorder (
       dc_id integer not null,
        dc_date datetime,
        dc_name integer not null,
        primary key (dc_id)
    ) engine=InnoDB;

    create table petshop_petseq (
       next_val bigint
    ) engine=InnoDB;

    insert into petshop_petseq values ( 1 );

    alter table petshop_pet 
       add constraint UK_PET_NAME unique (dc_name);

    alter table petshop_petorder 
       add constraint FK_PET_ORDER 
       foreign key (dc_name) 
       references petshop_pet (dc_id);
