
    create table petshop_pet (
       dc_id number(10,0) not null,
        dc_age number(10,0) not null,
        dc_name varchar2(128 char) not null,
        petType number(10,0) not null,
        dc_price number(10,0) not null,
        dc_reserved number(1,0) not null,
        dc_sex number(10,0) not null,
        dc_sold number(1,0) not null,
        primary key (dc_id)
    );

    create table petshop_petorder (
       dc_id number(10,0) not null,
        dc_date timestamp,
        dc_name number(10,0) not null,
        primary key (dc_id)
    );

    alter table petshop_pet 
       add constraint UK_PET_NAME unique (dc_name);

    alter table petshop_petorder 
       add constraint FK_PET_ORDER 
       foreign key (dc_name) 
       references petshop_pet;
