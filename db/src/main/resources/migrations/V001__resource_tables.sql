set search_path = test;

create type resource_locale as enum('default', 'de', 'en');

create table resources(
  "key" varchar(255) not null,
  locale resource_locale not null,
  body varchar(255) not null,
  created_at timestamp with time zone not null,
  modified_at timestamp with time zone not null
);

alter table resources add primary key ("key", locale);
