create sequence petshop_orderseq start 1 increment 4;
create sequence petshop_petseq start 1 increment 4;

    create table petshop_pet (
       dc_id int4 not null,
        dc_age int4 not null,
        dc_name varchar(128) not null,
        petType int4 not null,
        dc_price int4 not null,
        dc_reserved boolean not null,
        dc_sex int4 not null,
        dc_sold boolean not null,
        primary key (dc_id)
    );

    create table petshop_petorder (
       dc_id int4 not null,
        dc_date timestamp,
        dc_name int4 not null,
        primary key (dc_id)
    );

    alter table if exists petshop_pet 
       add constraint UK_PET_NAME unique (dc_name);

    alter table if exists petshop_petorder 
       add constraint FK_PET_ORDER 
       foreign key (dc_name) 
       references petshop_pet;
