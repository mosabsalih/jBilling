# Removing a whole entity
# Warning! this is not deleting all the international_description columns!
# see a complement query down this file
delete from contact_map where table_id = 10 and foreign_id in (select id from base_user where entity_id = XXX);
delete from contact_map where table_id = 5 and foreign_id = XXX;
delete from contact_field where contact_id not in ( select contact_id from contact_map); -- needs optmize with exists
delete from contact where id not in ( select contact_id from contact_map); -- needs optmize with exists
delete from preference where table_id = 10 and foreign_id in (select id from base_user where entity_id = XXX);
delete from customer where user_id in ( select id from base_user where entity_id = XXX);
delete from partner_payout  where partner_id in ( select p.id from partner p, base_user b where p.user_id = b.id and b.entity_id = XXX);
delete from partner where user_id in ( select id from base_user where entity_id = XXX);
delete from order_line  where order_id in ( select p.id from purchase_order p, base_user b where p.user_id = b.id and b.entity_id = XXX);
delete from order_process where order_id in ( select p.id from purchase_order p, base_user b where p.user_id = b.id and b.entity_id = XXX);
delete from purchase_order where user_id in ( select id from base_user where entity_id = XXX);
delete from payment_invoice where invoice_id in ( select i.id from invoice i, base_user b where i.user_id = b.id and entity_id = XXX);
delete from invoice_line  where invoice_id in ( select p.id from invoice p, base_user b where p.user_id = b.id and b.entity_id = XXX);
delete from invoice where user_id in ( select id from base_user where entity_id = XXX);
delete from partner_payout where payment_id in ( select p.id from payment p, base_user b where p.user_id = b.id and b.entity_id = XXX);
delete from payment_info_cheque where payment_id in ( select p.id from payment p, base_user b where p.user_id = b.id and b.entity_id = XXX);
delete from payment_authorization where payment_id in ( select p.id from payment p, base_user b where p.user_id = b.id and b.entity_id = XXX);
delete from payment where user_id in ( select id from base_user where entity_id = XXX);
create table id_temp ( id  integer);
insert into id_temp (id)
select credit_card_id from user_credit_card_map m, base_user b where m.user_id = b.id and b.entity_id = XXX;
delete from user_credit_card_map where user_id in ( select id from base_user where entity_id = XXX);
delete from credit_card  where id in ( select id from id_temp );
drop table id_temp;
delete from notification_message_arch_line  where message_archive_id in ( select p.id from notification_message_arch p, base_user b where p.user_id = b.id and b.entity_id = XXX);
delete from notification_message_arch where user_id in ( select id from base_user where entity_id = XXX);
delete from user_role_map where user_id in (select id from base_user where entity_id = XXX);
delete from event_log where user_id in (select id from base_user where entity_id = XXX);
delete from report_user where user_id in (select id from base_user where entity_id = XXX);
delete from permission_user where user_id in (select id from base_user where entity_id = XXX);
delete from item_user_price where user_id in (select id from base_user where entity_id = XXX);
delete from ach where user_id in (select id from base_user where entity_id = XXX);
delete from promotion_user_map where user_id in (select id from base_user where entity_id = XXX);
delete from base_user where entity_id = XXX;
delete from item_price where item_id in ( select id from item where entity_id = XXX);
delete from item_type_map where item_id in ( select id from item where entity_id = XXX);
delete from promotion where item_id in ( select id from item where entity_id = XXX);
delete from item_type_map where type_id in ( select id from item_type where entity_id = XXX);
delete from item where entity_id = XXX;
delete from item_type where entity_id = XXX;
delete from pluggable_task_parameter where task_id in ( select id from pluggable_task where entity_id = XXX);
delete from pluggable_task where entity_id = XXX;
delete from entity_delivery_method_map where entity_id = XXX;
delete from billing_process_configuration where entity_id = XXX;
delete from notification_message_line where message_section_id in ( select s.id from notification_message_section s, notification_message n where n.entity_id = XXX and s.message_id = n.id);
delete from notification_message_section where message_id in ( select id from notification_message where entity_id = XXX);
delete from notification_message where entity_id = XXX;
delete from process_run_total  where process_run_id in ( select pr.id from process_run pr, billing_process p where pr.process_id = p.id and p.entity_id = XXX);
delete from process_run where process_id in ( select id from billing_process where entity_id = XXX);
delete from billing_process where entity_id = XXX;
delete from order_period where entity_id = XXX;
delete from entity_payment_method_map where entity_id = XXX;
delete from event_log where entity_id = XXX;
delete from report_entity_map where entity_id = XXX;
delete from currency_entity_map where entity_id = XXX;
delete from ageing_entity_step where entity_id = XXX;
delete from contact_field where type_id in ( select id from contact_field_type where entity_id = XXX);
delete from contact_field_type where entity_id = XXX;
delete from preference where table_id = 5 and foreign_id = XXX;
delete from contact_map where type_id in (select id from contact_type where entity_id = XXX);
delete from contact_type where entity_id = XXX;
delete from list_field_entity where list_entity_id in ( select id from list_entity where entity_id = XXX);
delete from list_entity where entity_id = XXX;
delete from entity where id = XXX; 



#Getting all the text in a language
select table_id, name, foreign_id, psudo_column, content
from international_description i, betty_table b
where language_id = 4
  and b.id = i.table_id
  and table_id not in (69, 14, 24, 17, 28,13)
order by table_id;

# Finding the rows in international_description that are missing for a language
select table_id, name, foreign_id, psudo_column, content
from international_description i, betty_table b
where i.table_id = b.id
  and language_id = 1
  and table_id not in (69, 14, 24, 17, 28,13)
  and foreign_id not in (
    select foreign_id
      from international_description
     where language_id = 4
       and table_id = i.table_id );
       
# Calculating the number of transactions for an entity for a period of time
select count(*)
from payment p, base_user b
where p.user_id = b.id
  and b.entity_id = 301
  and p.result_id = 1
  and p.create_datetime between '2004-10-01' and '2004-10-22';      
  
# imorting prod db to devel
update contact set email = 'emilc@sapienter.com'; 
update pluggable_task_parameter set str_value = 'emilc@sapienter.com' where id = 160;
update credit_card set cc_number = '4111111111111111';
update billing_process_configuration set next_run_date = '2010-01-01', generate_report = 0, retries = 0, days_for_retry = null, auto_payment = 0;
update pluggable_task_parameter set int_value = null, str_value = null, float_value = null where name in ('bcc_to','from_name','smtp_server','from','username','password','port','reply_to');
update preference set int_value = 0 where type_id = 1;
update base_user set password = 'asdfasdf';
update notification_message set use_flag = 0;
update preference set str_value = null where type_id = 30;

# delegating an invoice to another invoice
select id, due_date, public_number ,balance from invoice where id in(sources...);
select next_id from betty_table where name = 'invoice_line';
update invoice set balance  = balance + TOTAL, total = total + TOTAL, carried_balance = carried_balance + TOTAL, to_process = 1 where id = TARGET;
update invoice set delegated_invoice_id = TARGET, to_process = 0, balance = 0 where id = SOURCE;
insert into invoice_line values (3503, TARGET, 3, TOTAL, null, null, 0, null, 'Invoice 115 due date 11/06/2004', null);

# creating a new menu help
insert into menu_option values (81, 'HELP|page=process|anchor=', 2, 3);
insert into international_description values (63, 81, 'display', 1, 'Help');
insert into permission values (121, 1, 81);
insert into permission_role_map values (121, 2);

#undeleteing users / purchase orders
update base_user set status_id=1, deleted = 0 where status_id != 1 or deleted = 1 and entity_id = 277;
insert into user_role_map select id, 5 from base_user where entity_id = 277 and id not in (select user_id from user_role_map);
update purchase_order set deleted = 0, status_id = 1 where user_id in (select id from base_user where entity_id = 277 and deleted = 0 and status_id = 1);

#enable a root user to use web services
insert into permission_user values (120, 1735, 1, 61);

#counting active customers for each entity
select count(*), entity_id 
from base_user bu, user_role_map urm
where deleted = 0 
  and status_id <= 4
  and bu.id = urm.user_id
  and urm.role_id = 5
group by entity_id
order by 2;
#number of invoices
select count(*), entity_id
  from base_user bu, invoice i
 where i.user_id = bu.id
   and i.deleted = 0
   and bu.deleted = 0
   and i.create_timestamp between '2007-10-01' and '2007-10-31'
   group by entity_id
   order by 2;

#find out the totals by payment method to recreate a billing_process_total_pm record
iselect sum(p.amount), method_id 
 from payment p, invoice i, payment_invoice_map m
 where i.billing_process_id = xxx
   and m.invoice_id = i.id
   and m.payment_id = p.id
   and p.result_id = 1
 group by method_id;

#create the init.sql file
#first: remove all the entities using the first script in this file
pg_dump -d jbilling -U jbilling_user -D -C -f init.sql
# add this to the script
create user jbilling_user password 'jbilling';
grant all on database jbilling to jbilling_user;

#use the script to initialize a fresh postgres install
psql -d template1 -h maximus -f src/sql/init.sql -U postgres

# delete unused international_description for a particuar table
delete from international_description where oid in (
select i.oid
from international_description i, jbilling_table t
where t.id = i.table_id
and t.name = 'report_type'
and not exists (
  select 1
    from report_type
   where id = i.foreign_id));
   
# more powerful: generates delete queries
select
'delete from international_description where oid in (
select i.oid
from international_description i, jbilling_table t
where t.id = i.table_id
and t.name = ''' || name || 
'''and not exists (
  select 1
    from ' || name ||
'   where id = i.foreign_id))\;'
from jbilling_table 
where exists (
  select 1
    from international_description
   where table_id = id
);

# show events history of a customer
\set userId 12345
select user_id, 'User', foreign_id, create_datetime, 
   case when module_id=1 then 'Billing Process' 
        when module_id=2 then 'User Maintenance'
        when module_id=3 then 'Item Maintenance'
        when module_id=4 then 'Item Type Maintenance'
        when module_id=5 then 'Item User Price Maintenance'
        when module_id=6 then 'Promotion Maintenance'
        when module_id=7 then 'Order Maintenance'
        when module_id=8 then 'Credit Card Maintenance'
        when module_id=9 then 'Invoice Maintenance'
        when module_id=10 then 'Payment Maintenance'
        when module_id=11 then 'Task Maintenance'
        when module_id=12 then 'Web services'
        when module_id=13 then 'Mediation'
    end,
    case when message_id=1 then 'Unbillend period'
         when message_id=2 then 'Not active yet' 
         when message_id=3 then 'One period needed' 
         when message_id=4 then 'Recently billed' 
         when message_id=5 then 'Wrong flag on' 
         when message_id=6 then 'Expired' 
         when message_id=10 then 'Review not approved' 
         when message_id=11 then 'Review not generated' 
         when message_id=8 then 'Password change' 
         when message_id=12 then 'Status change' 
         when message_id=14 then 'Not further step' 
         when message_id=15 then 'Cant pay partner' 
         when message_id=20 then 'Subscription status change' 
         when message_id=32 then 'Subscription status NOT change' 
         when message_id=21 then 'Account locked' 
         when message_id=13 then 'Order status change' 
         when message_id=17 then 'Order line updated' 
         when message_id=18 then 'Next invoice date updated' 
         when message_id=22 then 'Main subscription updated' 
         when message_id=24 then 'Payment instrument missing' 
         when message_id=16 then 'Invoice order applied' 
         when message_id=23 then 'Current order finished' 
         when message_id=25 then 'Row created' 
         when message_id=7 then 'Row deleted' 
         when message_id=9 then 'Row updated' 
         when message_id=19 then 'User transitions list' 
    end, 
old_num , old_str,old_date from event_log
where entity_id = 1
  and table_id = 10
  and foreign_id = :userId
union
select user_id, 'oRder', foreign_id, create_datetime,  
case when module_id=1 then 'Billing Process' 
        when module_id=2 then 'User Maintenance'
        when module_id=3 then 'Item Maintenance'
        when module_id=4 then 'Item Type Maintenance'
        when module_id=5 then 'Item User Price Maintenance'
        when module_id=6 then 'Promotion Maintenance'
        when module_id=7 then 'Order Maintenance'
        when module_id=8 then 'Credit Card Maintenance'
        when module_id=9 then 'Invoice Maintenance'
        when module_id=10 then 'Payment Maintenance'
        when module_id=11 then 'Task Maintenance'
        when module_id=12 then 'Web services'
        when module_id=13 then 'Mediation'
    end,
    case when message_id=1 then 'Unbillend period'
         when message_id=2 then 'Not active yet' 
         when message_id=3 then 'One period needed' 
         when message_id=4 then 'Recently billed' 
         when message_id=5 then 'Wrong flag on' 
         when message_id=6 then 'Expired' 
         when message_id=10 then 'Review not approved' 
         when message_id=11 then 'Review not generated' 
         when message_id=8 then 'Password change' 
         when message_id=12 then 'Status change' 
         when message_id=14 then 'Not further step' 
         when message_id=15 then 'Cant pay partner' 
         when message_id=20 then 'Subscription status change' 
         when message_id=32 then 'Subscription status NOT change' 
         when message_id=21 then 'Account locked' 
         when message_id=13 then 'Order status change' 
         when message_id=17 then 'Order line updated' 
         when message_id=18 then 'Next invoice date updated' 
         when message_id=22 then 'Main subscription updated' 
         when message_id=24 then 'Payment instrument missing' 
         when message_id=16 then 'Invoice order applied' 
         when message_id=23 then 'Current order finished' 
         when message_id=25 then 'Row created' 
         when message_id=7 then 'Row deleted' 
         when message_id=9 then 'Row updated' 
         when message_id=19 then 'User transitions list' 
    end, 
 old_num , old_str,old_date from event_log
where entity_id = 1
  and table_id = 21
  and foreign_id in (
  select id
   from purchase_order
  where user_id = :userId )
union
select user_id, 'inVoice', foreign_id, create_datetime,  
case when module_id=1 then 'Billing Process' 
        when module_id=2 then 'User Maintenance'
        when module_id=3 then 'Item Maintenance'
        when module_id=4 then 'Item Type Maintenance'
        when module_id=5 then 'Item User Price Maintenance'
        when module_id=6 then 'Promotion Maintenance'
        when module_id=7 then 'Order Maintenance'
        when module_id=8 then 'Credit Card Maintenance'
        when module_id=9 then 'Invoice Maintenance'
        when module_id=10 then 'Payment Maintenance'
        when module_id=11 then 'Task Maintenance'
        when module_id=12 then 'Web services'
        when module_id=13 then 'Mediation'
    end,
     case when message_id=1 then 'Unbillend period'
         when message_id=2 then 'Not active yet' 
         when message_id=3 then 'One period needed' 
         when message_id=4 then 'Recently billed' 
         when message_id=5 then 'Wrong flag on' 
         when message_id=6 then 'Expired' 
         when message_id=10 then 'Review not approved' 
         when message_id=11 then 'Review not generated' 
         when message_id=8 then 'Password change' 
         when message_id=12 then 'Status change' 
         when message_id=14 then 'Not further step' 
         when message_id=15 then 'Cant pay partner' 
         when message_id=20 then 'Subscription status change' 
         when message_id=32 then 'Subscription status NOT change' 
         when message_id=21 then 'Account locked' 
         when message_id=13 then 'Order status change' 
         when message_id=17 then 'Order line updated' 
         when message_id=18 then 'Next invoice date updated' 
         when message_id=22 then 'Main subscription updated' 
         when message_id=24 then 'Payment instrument missing' 
         when message_id=16 then 'Invoice order applied' 
         when message_id=23 then 'Current order finished' 
         when message_id=25 then 'Row created' 
         when message_id=7 then 'Row deleted' 
         when message_id=9 then 'Row updated' 
         when message_id=19 then 'User transitions list' 
    end, 
 old_num , old_str,old_date from event_log
where entity_id = 1
  and table_id = 39
  and foreign_id in (
  select id
   from invoice
  where user_id = :userId )
union
select user_id, 'payMent', foreign_id, create_datetime,  
case when module_id=1 then 'Billing Process' 
        when module_id=2 then 'User Maintenance'
        when module_id=3 then 'Item Maintenance'
        when module_id=4 then 'Item Type Maintenance'
        when module_id=5 then 'Item User Price Maintenance'
        when module_id=6 then 'Promotion Maintenance'
        when module_id=7 then 'Order Maintenance'
        when module_id=8 then 'Credit Card Maintenance'
        when module_id=9 then 'Invoice Maintenance'
        when module_id=10 then 'Payment Maintenance'
        when module_id=11 then 'Task Maintenance'
        when module_id=12 then 'Web services'
        when module_id=13 then 'Mediation'
    end,
    case when message_id=1 then 'Unbillend period'
         when message_id=2 then 'Not active yet' 
         when message_id=3 then 'One period needed' 
         when message_id=4 then 'Recently billed' 
         when message_id=5 then 'Wrong flag on' 
         when message_id=6 then 'Expired' 
         when message_id=10 then 'Review not approved' 
         when message_id=11 then 'Review not generated' 
         when message_id=8 then 'Password change' 
         when message_id=12 then 'Status change' 
         when message_id=14 then 'Not further step' 
         when message_id=15 then 'Cant pay partner' 
         when message_id=20 then 'Subscription status change' 
         when message_id=32 then 'Subscription status NOT change' 
         when message_id=21 then 'Account locked' 
         when message_id=13 then 'Order status change' 
         when message_id=17 then 'Order line updated' 
         when message_id=18 then 'Next invoice date updated' 
         when message_id=22 then 'Main subscription updated' 
         when message_id=24 then 'Payment instrument missing' 
         when message_id=16 then 'Invoice order applied' 
         when message_id=23 then 'Current order finished' 
         when message_id=25 then 'Row created' 
         when message_id=7 then 'Row deleted' 
         when message_id=9 then 'Row updated' 
         when message_id=19 then 'User transitions list' 
    end, 
 old_num , old_str,old_date from event_log
where entity_id = 1
  and table_id = 42
  and foreign_id in (
  select id
   from payment
  where user_id = :userId )
order by create_datetime;

-- list of processes
select p.billing_date, p.retries_to_do, r.*, finished - started
 from billing_process p, process_run r
 where p.id = r.process_id
 order by p.billing_date desc, started desc;

-- update an entry of  jbilling_seqs
update jbilling_seqs
set next_id = ( select (max(id)/10)+1 from base_user )
where name = 'base_user';

-- list the statuses for a generic table. Just replace 'process_run_status'
select gs.id, gs.status_value, content
from international_description id, jbilling_table jt, generic_status gs
where jt.name = gs.dtype
  and id.table_id = jt.id
  and gs.dtype = 'process_run_status'
  and id.foreign_id = gs.status_value

