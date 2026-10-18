alter table expenses add column created_by_id bigint;

alter table expenses add constraint fk_expenses_created_by foreign key (created_by_id) references app_users (id);
