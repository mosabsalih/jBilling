-- this script will upgrade a database schema from the latest jbilling release
-- to the code currently at the tip of the trunk.
-- It is tested on postgreSQL, but it is meant to be ANSI SQL
--
-- MySQL does not support many of the ANSI SQL statements used in this file to upgrade the
-- base schema. If you are using MySQL as your database, you will need to edit this file and
-- comment out the labeled 'postgresql' statements and un-comment the ones labeled 'mysql'


-- external ACH storage plug-in
insert into pluggable_task_type  (id, category_id, class_name, min_parameters) values (84, 17, 'com.sapienter.jbilling.server.payment.tasks.SaveACHExternallyTask', 1);

-- Modified size of ACH records to allow encryption
alter table ach alter column aba_routing type character varying(40); -- postgresql
alter table ach alter column bank_account type character varying(60); -- postgresql
-- alter table ach modify aba_routing varchar(40); -- mysql
-- alter table ach modify bank_account varchar(60); -- mysql

-- payment authorization transaction id
ALTER TABLE payment_authorization ALTER COLUMN transaction_id TYPE character varying(40); -- postgresql
-- alter table payment_authorization modify transaction_id varchar(40); -- mysql

-- ach external storage gateway_key
alter table ach add column gateway_key varchar(100) default null;

-- one-time / recurring invoice line types
insert into invoice_line_type (id, description, order_position) values (6, 'item one-time', 3);
update invoice_line_type set description = 'item recurring' where id = 1;
update invoice_line_type set order_position = 4 where id = 4;
update invoice_line_type set order_position = 5 where id = 5;
update invoice_line_type set order_position = 6 where id = 2;

-- new billing process filter task 
insert into pluggable_task_type values (85, 20, 'com.sapienter.jbilling.server.process.task.BillableUserOrdersBillingProcessFilterTask', 0);

-- new notification categories for ui
create table notification_category (
	id integer NOT NULL,
	CONSTRAINT notification_category_pk PRIMARY KEY(id)
);

insert into notification_category (id) values (1);
insert into notification_category (id) values (2);
insert into notification_category (id) values (3);
insert into notification_category (id) values (4);

insert into jbilling_table (id, name) values (104, 'notification_category');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (104, 1, 'description',1, 'Invoices');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (104, 2, 'description',1, 'Orders');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (104, 3, 'description',1, 'Payments');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (104, 4, 'description',1, 'Users');

-- new column to store the notification category for this notification
ALTER table notification_message_type add column category_id integer;
ALTER table notification_message_type add constraint "category_id_fk_1" foreign key (category_id) references notification_category(id); --postgres
-- alter table notification_message_type add constraint category_id_fk_1 foreign key (category_id) references notification_category(id); -- mysql

update notification_message_type set category_id = 1 where id = 1;
update notification_message_type set category_id = 4 where id = 2;
update notification_message_type set category_id = 4 where id = 3;
update notification_message_type set category_id = 4 where id = 4;
update notification_message_type set category_id = 4 where id = 5;
update notification_message_type set category_id = 4 where id = 6;
update notification_message_type set category_id = 4 where id = 7;
update notification_message_type set category_id = 4 where id = 8;
update notification_message_type set category_id = 4 where id = 9;
update notification_message_type set category_id = 3 where id = 10;
update notification_message_type set category_id = 3 where id = 11;
update notification_message_type set category_id = 1 where id = 12;
update notification_message_type set category_id = 2 where id = 13;
update notification_message_type set category_id = 2 where id = 14;
update notification_message_type set category_id = 2 where id = 15;
update notification_message_type set category_id = 3 where id = 16;
update notification_message_type set category_id = 3 where id = 17;
update notification_message_type set category_id = 1 where id = 18;
update notification_message_type set category_id = 4 where id = 19;
update notification_message_type set category_id = 4 where id = 20;

-- plug-in categories now have a i18n description
alter table pluggable_task_type_category drop column description;
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 1, 'description',1, 'Item management and order line total calculation');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 2, 'description',1, 'Billing process: order filters');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 3, 'description',1, 'Billing process: invoice filters');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 4, 'description',1, 'Invoice presentation');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 5, 'description',1, 'Billing process: order periods calculation');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 6, 'description',1, 'Payment gateway integration');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 7, 'description',1, 'Notifications');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 8, 'description',1, 'Payment instrument selection');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 9, 'description',1, 'Penalties for overdue invoices');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 10, 'description',1, 'Alarms when a payment gateway is down');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 11, 'description',1, 'Subscription status manager');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 12, 'description',1, 'Parameters for asynchronous payment processing');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 13, 'description',1, 'Add one product to order');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 14, 'description',1, 'Product pricing');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 15, 'description',1, 'Mediation Reader');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 16, 'description',1, 'Mediation Processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 17, 'description',1, 'Generic internal events listener');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 18, 'description',1, 'External provisioning processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 19, 'description',1, 'Purchase validation against pre-paid balance / credit limit');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 20, 'description',1, 'Billing process: customer selection');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 21, 'description',1, 'Mediation Error Handler');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 22, 'description',1, 'Scheduled Plug-ins');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 23, 'description',1, 'Rules Generators');

ALTER table pluggable_task add column notes varchar(1000);

-- add descriptions to every plug-in type, so the new GUI can use them
delete from international_description where table_id = 24;
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 1, 'title',1, 'Default order totals');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 1, 'description',1, 'Calculates the order total and the total for each line, considering the item prices, the quantity and if the prices are percentage or not.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 2, 'title',1, 'VAT');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 2, 'description',1, 'Adds an additional line to the order with a percentage charge to represent the value added tax.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 3, 'title',1, 'Invoice due date');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 3, 'description',1, 'A very simple implementation that sets the due date of the invoice. The due date is calculated by just adding the period of time to the invoice date.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 4, 'title',1, 'Default invoice composition.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 4, 'description',1, 'This task will copy all the lines on the orders and invoices to the new invoice, considering the periods involved for each order, but not the fractions of periods. It will not copy the lines that are taxes. The quantity and total of each line will be multiplied by the amount of periods.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 5, 'title',1, 'Standard Order Filter');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  5, 'description',1, 'Decides if an order should be included in an invoice for a given billing process.  This is done by taking the billing process time span, the order period, the active since/until, etc.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  6, 'title',1, 'Standard Invoice Filter');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  6, 'description',1, 'Always returns true, meaning that the overdue invoice will be carried over to a new invoice.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  7, 'title',1, 'Default Order Periods');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  7, 'description',1, 'Calculates the start and end period to be included in an invoice. This is done by taking the billing process time span, the order period, the active since/until, etc.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  8, 'title',1, 'Authorize.net payment processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  8, 'description',1, 'Integration with the authorize.net payment gateway.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  9, 'title',1, 'Standard Email Notification');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  9, 'description',1, 'Notifies a user by sending an email. It supports text and HTML emails');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  10, 'title',1, 'Default payment information');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  10, 'description',1, 'Finds the information of a payment method available to a customer, given priority to credit card. In other words, it will return the credit car of a customer or the ACH information in that order.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  11, 'title',1, 'Testing plug-in for partner payouts');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  11, 'description',1, 'Plug-in useful only for testing');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  12, 'title',1, 'PDF invoice notification');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  12, 'description',1, 'Will generate a PDF version of an invoice.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  14, 'title',1, 'No invoice carry over');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  14, 'description',1, 'Returns always false, which makes jBilling to never carry over an invoice into another newer invoice.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  15, 'title',1, 'Default interest task');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  15, 'description',1, 'Will create a new order with a penalty item. The item is taken as a parameter to the task.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  16, 'title',1, 'Anticipated order filter');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  16, 'description',1, 'Extends BasicOrderFilterTask, modifying the dates to make the order applicable a number of months before it would be by using the default filter.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  17, 'title',1, 'Anticipate order periods.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  17, 'description',1, 'Extends BasicOrderPeriodTask, modifying the dates to make the order applicable a number of months before itd be by using the default task.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  19, 'title',1, 'Email & process authorize.net');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  19, 'description',1, 'Extends the standard authorize.net payment processor to also send an email to the company after processing the payment.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  20, 'title',1, 'Payment gateway down alarm');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  20, 'description',1, 'Sends an email to the billing administrator as an alarm when a payment gateway is down.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  21, 'title',1, 'Test payment processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  21, 'description',1, 'A test payment processor implementation to be able to test jBillings functions without using a real payment gateway.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  22, 'title',1, 'Router payment processor based on Custom Fields');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  22, 'description',1, 'Allows a customer to be assigned a specific payment gateway. It checks a custom contact field to identify the gateway and then delegates the actual payment processing to another plugin.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  23, 'title',1, 'Default subscription status manager');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  23, 'description',1, 'It determines how a payment event affects the subscription status of a user, considering its present status and a state machine.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  24, 'title',1, 'ACH Commerce payment processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  24, 'description',1, 'Integration with the ACH commerce payment gateway.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  25, 'title',1, 'Standard asynchronous parameters');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  25, 'description',1, 'A dummy task that does not add any parameters for asynchronous payment processing. This is the default.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  26, 'title',1, 'Router asynchronous parameters');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  26, 'description',1, 'This plug-in adds parameters for asynchronous payment processing to have one processing message bean per payment processor. It is used in combination with the router payment processor plug-ins.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  28, 'title',1, 'Standard Item Manager');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  28, 'description',1, 'It adds items to an order. If the item is already in the order, it only updates the quantity.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  29, 'title',1, 'Rules Item Manager');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  29, 'description',1, 'This is a rules-based plug-in. It will do what the basic item manager does (actually calling it); but then it will execute external rules as well. These external rules have full control on changing the order that is getting new items.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  30, 'title',1, 'Rules Line Total');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  30, 'description',1, 'This is a rules-based plug-in. It calculates the total for an order line (typically this is the price multiplied by the quantity); allowing for the execution of external rules.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  31, 'title',1, 'Rules Pricing');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  31, 'description',1, 'This is a rules-based plug-in. It gives a price to an item by executing external rules. You can then add logic externally for pricing. It is also integrated with the mediation process by having access to the mediation pricing data.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  32, 'title',1, 'Separator file reader');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  32, 'description',1, 'This is a reader for the mediation process. It reads records from a text file whose fields are separated by a character (or string).');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  33, 'title',1, 'Rules mediation processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  33, 'description',1, 'This is a rules-based plug-in (see chapter 7). It takes an event record from the mediation process and executes external rules to translate the record into billing meaningful data. This is at the core of the mediation component, see the “Telecom Guide” document for more information.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  34, 'title',1, 'Fixed length file reader');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  34, 'description',1, 'This is a reader for the mediation process. It reads records from a text file whose fields have fixed positions,and the record has a fixed length.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  35, 'title',1, 'Payment information without validation');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  35, 'description',1, 'This is exactly the same as the standard payment information task, the only difference is that it does not validate if the credit card is expired. Use this plug-in only if you want to submit payment with expired credit cards.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  36, 'title',1, 'Notification task for testing');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  36, 'description',1, 'This plug-in is only used for testing purposes. Instead of sending an email (or other real notification); it simply stores the text to be sent in a file named emails_sent.txt.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  37, 'title',1, 'Order periods calculator with pro rating.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  37, 'description',1, 'This plugin takes into consideration the field cycle starts of orders to calculate fractional order periods.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  38, 'title',1, 'Invoice composition task with pro-rating (day as fraction)');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  38, 'description',1, 'When creating an invoice from an order, this plug-in will pro-rate any fraction of a period taking a day as the smallest billable unit.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  39, 'title',1, 'Payment process for the Intraanuity payment gateway');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  39, 'description',1, 'Integration with the Intraanuity payment gateway.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  40, 'title',1, 'Automatic cancellation credit.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  40, 'description',1, 'This plug-in will create a new order with a negative price to reflect a credit when an order is canceled within a period that has been already invoiced.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  41, 'title',1, 'Fees for early cancellation of a plan.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  41, 'description',1, 'This plug-in will use external rules to determine if an order that is being canceled should create a new order with a penalty fee. This is typically used for early cancels of a contract.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  42, 'title',1, 'Blacklist filter payment processor.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  42, 'description',1, 'Used for blocking payments from reaching real payment processors. Typically configured as first payment processor in the processing chain.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  43, 'title',1, 'Blacklist user when their status becomes suspended or higher.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  43, 'description',1, 'Causes users and their associated details (e.g., credit card number, phone number, etc.) to be blacklisted when their status becomes suspended or higher. ');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  44, 'title',1, 'JDBC Mediation Reader.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  44, 'description',1, 'This is a reader for the mediation process. It reads records from a JDBC database source.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  45, 'title',1, 'MySQL Mediation Reader.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  45, 'description',1, 'This is a reader for the mediation process. It is an extension of the JDBC reader, allowing easy configuration of a MySQL database source.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  46, 'title',1, 'Provisioning commands rules task.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  46, 'description',1, 'Responds to order related events. Runs rules to generate commands to send via JMS messages to the external provisioning module.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  47, 'title',1, 'Test external provisioning task.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  47, 'description',1, 'This plug-in is only used for testing purposes. It is a test external provisioning task for testing the provisioning modules.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  48, 'title',1, 'CAI external provisioning task.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  48, 'description',1, 'An external provisioning plug-in for communicating with the Ericsson Customer Administration Interface (CAI).');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  49, 'title',1, 'Currency Router payment processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  49, 'description',1, 'Delegates the actual payment processing to another plug-in based on the currency of the payment.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  50, 'title',1, 'MMSC external provisioning task.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  50, 'description',1, 'An external provisioning plug-in for communicating with the TeliaSonera MMSC.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  51, 'title',1, 'Filters out negative invoices for carry over.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  51, 'description',1, 'This filter will only invoices with a positive balance to be carried over to the next invoice.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  52, 'title',1, 'File invoice exporter.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  52, 'description',1, 'It will generate a file with one line per invoice generated.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  53, 'title',1, 'Rules caller on an event.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  53, 'description',1, 'It will call a package of rules when an internal event happens.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  54, 'title',1, 'Dynamic balance manager');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  54, 'description',1, 'It will update the dynamic balance of a customer (pre-paid or credit limit) when events affecting the balance happen.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  55, 'title',1, 'Balance validator based on the customer balance.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  55, 'description',1, 'Used for real-time mediation, this plug-in will validate a call based on the current dynamic balance of a customer.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  56, 'title',1, 'Balance validator based on rules.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  56, 'description',1, 'Used for real-time mediation, this plug-in will validate a call based on a package or rules');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  57, 'title',1, 'Payment processor for Payments Gateway.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  57, 'description',1, 'Integration with the Payments Gateway payment processor.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  58, 'title',1, 'Credit cards are stored externally.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  58, 'description',1, 'Saves the credit card information in the payment gateway, rather than the jBilling DB.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  59, 'title',1, 'Rules Item Manager 2');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  59, 'description',1, 'This is a rules-based plug-in compatible with the mediation module of jBilling 2.2.x. It will do what the basic item manager does (actually calling it); but then it will execute external rules as well. These external rules have full control on changing the order that is getting new items.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  60, 'title',1, 'Rules Line Total - 2');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  60, 'description',1, 'This is a rules-based plug-in, compatible with the mediation process of jBilling 2.2.x and later. It calculates the total for an order line (typically this is the price multiplied by the quantity); allowing for the execution of external rules.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  61, 'title',1, 'Rules Pricing 2');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  61, 'description',1, 'This is a rules-based plug-in compatible with the mediation module of jBilling 2.2.x. It gives a price to an item by executing external rules. You can then add logic externally for pricing. It is also integrated with the mediation process by having access to the mediation pricing data.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  63, 'title',1, 'Test payment processor for external storage.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  63, 'description',1, 'A fake plug-in to test payments that would be stored externally.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  64, 'title',1, 'WorldPay integration');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  64, 'description',1, 'Payment processor plug-in to integrate with RBS WorldPay');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  65, 'title',1, 'WorldPay integration with external storage');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  65, 'description',1, 'Payment processor plug-in to integrate with RBS WorldPay. It stores the credit card information (number, etc) in the gateway.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  66, 'title',1, 'Auto recharge');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  66, 'description',1, 'Monitors the balance of a customer and upon reaching a limit, it requests a real-time payment');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  67, 'title',1, 'Beanstream gateway integration');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  67, 'description',1, 'Payment processor for integration with the Beanstream payment gateway');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  68, 'title',1, 'Sage payments gateway integration');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  68, 'description',1, 'Payment processor for integration with the Sage payment gateway');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  69, 'title',1, 'Standard billing process users filter');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  69, 'description',1, 'Called when the billing process runs to select which users to evaluate. This basic implementation simply returns every user not in suspended (or worse) status');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  70, 'title',1, 'Selective billing process users filter');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  70, 'description',1, 'Called when the billing process runs to select which users to evaluate. This only returns users with orders that have a next invoice date earlier than the billing process.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  71, 'title',1, 'Mediation file error handler');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  71, 'description',1, 'Event records with errors are saved to a file');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  73, 'title',1, 'Mediation data base error handler');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  73, 'description',1, 'Event records with errors are saved to a database table');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  75, 'title',1, 'Paypal integration with external storage');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  75, 'description',1, 'Submits payments to paypal as a payment gateway and stores credit card information in PayPal as well');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  76, 'title',1, 'Authorize.net integration with external storage');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  76, 'description',1, 'Submits payments to authorize.net as a payment gateway and stores credit card information in authorize.net as well');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  77, 'title',1, 'Payment method router payment processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  77, 'description',1, 'Delegates the actual payment processing to another plug-in based on the payment method of the payment.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  78, 'title',1, 'Dynamic rules generator');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  78, 'description',1, 'Generates rules dynamically based on a Velocity template.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  79, 'title',1, 'Mediation Process Task');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  79, 'description',1, 'A scheduled task to execute the Mediation Process.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  80, 'title',1, 'Billing Process Task');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24,  80, 'description',1, 'A scheduled task to execute the Billing Process.');

-- filter database tables
drop table if exists filter;
create table filter (
    id int not null,
    filter_set_id int not null,
    type varchar(255) not null,
    constraint_type varchar(255) not null,
    field varchar(255) not null,
    template varchar(255) not null,
    visible boolean not null,
    integer_value int,
    string_value varchar(255),
    start_date_value timestamp,
    end_date_value timestamp,
    version int not null,
    primary key (id)
);

drop table if exists filter_set;
create table filter_set (
    id int not null,
    name varchar(255) not null,
    user_id int not null,
    version int not null,    
    primary key (id)
);

insert into jbilling_seqs (name, next_id) values ('filter', 1);
insert into jbilling_seqs (name, next_id) values ('filter_set', 1);

-- recent item tables
drop table if exists recent_item;
create table recent_item (
  id int not null,  
  type varchar(255) not null,
  object_id int not null,
  user_id int not null,
  version int not null,
  primary key (id)
);

insert into jbilling_seqs (name, next_id) values ('recent_item', 1);

-- breadcrumb tables
drop table if exists breadcrumb;
create table breadcrumb (
    id int not null,
    user_id int not null,
    controller varchar(255) not null,
    action varchar(255),
    name varchar(255),
    object_id int,
    version int not null,
    primary key (id)
);

insert into jbilling_seqs (name, next_id) values ('breadcrumb', 1);

-- contact type optlock
alter table contact_type add column OPTLOCK int null;
update contact_type set OPTLOCK = 0;
alter table contact_type alter column OPTLOCK set not null;

-- Orders should always have an active since date
update purchase_order set active_since = create_datetime where active_since is null;

-- custom contact fields
-- table id entry
INSERT INTO jbilling_table(name, id) VALUES ('contact_field_type', 99);

-- table id generator
INSERT INTO jbilling_seqs(name, next_id) VALUES ('contact_field_type', 10);

-- optlock column
alter table contact_field_type  add column OPTLOCK int null;
update contact_field_type set OPTLOCK = 0; 
alter table contact_field_type alter column OPTLOCK set not null;

-- Entries to bring the old contact field type descriptions from the properties file
-- into the international_description table for i18n purposes.
-- The below query should be made once per properties file. In the query below:
--    1. 'content' value comes from the property value in the language properties file
--    2. 'foreign_id' is the row id of the contact_field_type' table
--    3. 'language_id' is the id column value of the language table for the corresponding language
--	   4. 'table_id' is the id columne value of the jbilling_table table where name = 'contact_field_type'

insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (99, 1, 'description', 1, 'Referral Fee');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (99, 2, 'description', 1, 'Payment Processor');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (99, 3, 'description', 1, 'IP Address');

-- descriptions of messages for the audit log screens
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 20, 'description', 1, 'User subscription status has changed');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 32, 'description', 1, 'User subscription status has NOT changed');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 21, 'description', 1, 'User account is now locked');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 33, 'description', 1, 'The dynamic balance of a user has changed');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 34, 'description', 1, 'The invoice if child flag has changed');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 17, 'description', 1, 'The order line has been updated');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 18, 'description', 1, 'The order next billing date has been changed');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 22, 'description', 1, 'The order main subscription flag was changed');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 26, 'description', 1, 'An invoiced order was cancelled, a credit order was created');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 24, 'description', 1, 'A valid payment method was not found. The payment request was cancelled');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 23, 'description', 1, 'All the one-time orders the mediation found were in status finished');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 27, 'description', 1, 'A user id was added to the blacklist');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 28, 'description', 1, 'A user id was removed from the blacklist');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 29, 'description', 1, 'Posted a provisioning command using a UUID');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 30, 'description', 1, 'A command was posted for provisioning');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 31, 'description', 1, 'The provisioning status of an order line has changed');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 25, 'description', 1, 'A new row has been created');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (47, 19, 'description', 1, 'Last API call to get the the user subscription status transitions');

-- lengthen the preference int value to allow for longer mediation "last read ID" values
-- alter table preference modify int_value int4 null default null; -- mysql
alter table preference alter int_value type int4; -- postgresql

-- shortcut tables
drop table if exists shortcut;
CREATE TABLE shortcut (
  id int NOT NULL,
  user_id int NOT NULL,
  controller character varying(255) NOT NULL,
  action character varying(255),
  name character varying(255),
  object_id int,
  version int not null,
  PRIMARY KEY (id)
);

insert into jbilling_seqs values ('shortcut', 1);

-- gl code new field in item table
alter table item add column gl_code character varying (50);

-- drop legacy reporting tables
drop table report_field;
drop table report_type_map;
drop table report_type;
drop table report_user;
drop table report_entity_map;
drop table report;

delete from jbilling_seqs where name in ('report_field', 'report_type_map', 'report_type', 'report_user', 'report_entity_map', 'report');
delete from international_description where table_id in (
  select id from jbilling_table where name in ('report_field', 'report_type_map', 'report_type', 'report_user', 'report_entity_map', 'report')
);
delete from jbilling_table where name in ('report_field', 'report_type_map', 'report_type', 'report_user', 'report_entity_map', 'report');

-- new reports tables
drop table if exists report;
create table report (
    id int NOT NULL,
    type_id int NOT NULL,
    name varchar(255) NOT NULL,
    file_name varchar(255) NOT NULL,
    OPTLOCK int NOT NULL,
    PRIMARY KEY (id)
);

drop table if exists report_type;
create table report_type (
    id int NOT NULL,
    name varchar(255) NOT NULL,
    OPTLOCK int NOT NULL,
    PRIMARY KEY (id)
);
alter table report add constraint report_type_id_FK foreign key (type_id) references report_type (id);

drop table if exists report_parameter;
create table report_parameter (
    id int NOT NULL,
    report_id int NOT NULL,
    dtype varchar(10) NOT NULL,
    name varchar(255) NOT NULL,
    PRIMARY KEY (id)
);
alter table report_parameter add constraint report_param_report_id_FK foreign key (report_id) references report (id);

drop table if exists entity_report_map;
create table entity_report_map (
    report_id int NOT NULL,
    entity_id int NOT NULL,
    PRIMARY KEY (report_id, entity_id)
);
alter table entity_report_map add constraint report_map_report_id_FK foreign key (report_id) references report (id);
alter table entity_report_map add constraint report_map_entity_id_FK foreign key (entity_id) references entity (id);

insert into jbilling_table (id, name) values (100, 'report');
insert into jbilling_table (id, name) values (101, 'report_type');
insert into jbilling_table (id, name) values (102, 'report_parameter');

insert into jbilling_seqs (name, next_id) values ('report', 1);
insert into jbilling_seqs (name, next_id) values ('report_type', 1);
insert into jbilling_seqs (name, next_id) values ('report_parameter', 1);

-- new report types
insert into report_type (id, name, optlock) values (1, 'invoice', 0);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (101, 1, 'description', 1, 'Invoice Reports');

insert into report_type (id, name, optlock) values (2, 'order', 0);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (101, 2, 'description', 1, 'Order Reports');

insert into report_type (id, name, optlock) values (3, 'payment', 0);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (101, 3, 'description', 1, 'Payment Reports');

insert into report_type (id, name, optlock) values (4, 'user', 0);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (101, 4, 'description', 1, 'User Reports');

-- invoice reports
insert into report (id, type_id, name, file_name, optlock) values (1, 1, 'total_invoiced', 'total_invoiced.jasper', 0);
insert into report_parameter (id, report_id, dtype, name) values (1, 1, 'date', 'start_date');
insert into report_parameter (id, report_id, dtype, name) values (2, 1, 'date', 'end_date');
insert into report_parameter (id, report_id, dtype, name) values (3, 1, 'integer', 'period');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 1, 'description', 1, 'Total amount invoiced grouped by period.');
insert into entity_report_map (report_id, entity_id) values (1, 1);

insert into report (id, type_id, name, file_name, optlock) values (2, 1, 'ageing_balance', 'ageing_balance.jasper', 0);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 2, 'description', 1, 'Detailed balance ageing report. Shows the age of outstanding customer balances.');
insert into entity_report_map (report_id, entity_id) values (2, 1);

-- order reports
insert into report (id, type_id, name, file_name, optlock) values (3, 2, 'product_subscribers', 'product_subscribers.jasper', 0);
insert into report_parameter (id, report_id, dtype, name) values (4, 3, 'integer', 'item_id');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 3, 'description', 1, 'Number of users subscribed to a specific product.');
insert into entity_report_map (report_id, entity_id) values (3, 1);

-- payment reports
insert into report (id, type_id, name, file_name, optlock) values (4, 3, 'total_payments', 'total_payments.jasper', 0);
insert into report_parameter (id, report_id, dtype, name) values (5, 4, 'date', 'start_date');
insert into report_parameter (id, report_id, dtype, name) values (6, 4, 'date', 'end_date');
insert into report_parameter (id, report_id, dtype, name) values (7, 4, 'integer', 'period');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 4, 'description', 1, 'Total payment amount received grouped by period.');
insert into entity_report_map (report_id, entity_id) values (4, 1);

-- user reports
insert into report (id, type_id, name, file_name, optlock) values (5, 4, 'user_signups', 'user_signups.jasper', 0);
insert into report_parameter (id, report_id, dtype, name) values (8, 5, 'date', 'start_date');
insert into report_parameter (id, report_id, dtype, name) values (9, 5, 'date', 'end_date');
insert into report_parameter (id, report_id, dtype, name) values (10, 5, 'integer', 'period');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 5, 'description', 1, 'Number of customers created within a period.');
insert into entity_report_map (report_id, entity_id) values (5, 1);

insert into report (id, type_id, name, file_name, optlock) values (6, 4, 'top_customers', 'top_customers.jasper', 0);
insert into report_parameter (id, report_id, dtype, name) values (11, 6, 'date', 'start_date');
insert into report_parameter (id, report_id, dtype, name) values (12, 6, 'date', 'end_date');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 6, 'description', 1, 'Total revenue (sum of received payments) per customer.');
insert into entity_report_map (report_id, entity_id) values (6, 1);

-- finance reports
insert into report (id, type_id, name, file_name, optlock) values (7, 1, 'accounts_receivable', 'accounts_receivable.jasper', 0);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 7, 'description', 1, 'Simple accounts receivable report showing current account balances.');
insert into entity_report_map (report_id, entity_id) values (7, 1);

insert into report (id, type_id, name, file_name, optlock) values (8, 1, 'gl_detail', 'gl_detail.jasper', 0);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 8, 'description', 1, 'General ledger details of all invoiced charges for the given day.');
insert into report_parameter (id, report_id, dtype, name) values (13, 8, 'date', 'date');
insert into entity_report_map (report_id, entity_id) values (8, 1);

insert into report (id, type_id, name, file_name, optlock) values (9, 1, 'gl_summary', 'gl_summary.jasper', 0);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (100, 9, 'description', 1, 'General ledger summary of all invoiced charges for the given day, grouped by item type.');
insert into report_parameter (id, report_id, dtype, name) values (14, 9, 'date', 'date');
insert into entity_report_map (report_id, entity_id) values (9, 1);

-- preference value consolidation
update preference set str_value = int_value where int_value is not null;
update preference set str_value = float_value where float_value is not null;

alter table preference drop column int_value;
alter table preference drop column float_value;

alter table preference rename column str_value to value; -- postgresql
-- alter table preference change str_value value varchar(200); -- mysql

update preference_type set str_def_value = int_def_value where int_def_value is not null;
update preference_type set str_def_value = float_def_value where float_def_value is not null;

alter table preference_type drop column int_def_value;
alter table preference_type drop column float_def_value;

alter table preference_type rename column str_def_value to def_value; -- postgresql
-- alter table preference_type change str_def_value def_value varchar(200); -- mysql


-- remove obsolete preferences
delete from preference where type_id = 1;
delete from preference where type_id = 2;
delete from preference where type_id = 3;
delete from preference where type_id = 26;
delete from preference where type_id = 34;

delete from international_description where table_id = 50 and foreign_id = 1;
delete from international_description where table_id = 50 and foreign_id = 2;
delete from international_description where table_id = 50 and foreign_id = 3;
delete from international_description where table_id = 50 and foreign_id = 26;
delete from international_description where table_id = 50 and foreign_id = 34;

delete from preference_type where id = 1;
delete from preference_type where id = 2;
delete from preference_type where id = 3;
delete from preference_type where id = 26;
delete from preference_type where id = 34;


-- missing preference descriptions
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 25, 'description', 1, 'Use overdue penalties (interest).');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 27, 'description', 1, 'Use order anticipation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 28, 'description', 1, 'Paypal account.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 29, 'description', 1, 'Paypal button URL.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 30, 'description', 1, 'URL for HTTP ageing callback.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 31, 'description', 1, 'Use continuous invoice dates.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 32, 'description', 1, 'Attach PDF invoice to email notification.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 33, 'description', 1, 'Force one order per invoice.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 35, 'description', 1, 'Add order Id to invoice lines.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 36, 'description', 1, 'Allow customers to edit own contact information.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 37, 'description', 1, 'Hide (mask) credit card numbers.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 38, 'description', 1, 'Link ageing to customer subscriber status.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 39, 'description', 1, 'Lock-out user after failed login attempts.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 40, 'description', 1, 'Expire user passwords after days.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 41, 'description', 1, 'Use main-subscription orders.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 42, 'description', 1, 'Use pro-rating.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 43, 'description', 1, 'Use payment blacklist.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 44, 'description', 1, 'Allow negative payments.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 45, 'description', 1, 'Delay negative invoice payments.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 46, 'description', 1, 'Allow invoice without orders.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 47, 'description', 1, 'Last read mediation record id.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 48, 'description', 1, 'Use provisioning.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 49, 'description', 1, 'Automatic customer recharge threshold.');


-- preference instructions
insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 4, 'instruction', 1, 'Grace period in days before ageing a customer with an overdue invoice.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 5, 'instruction', 1, 'Partner default percentage commission rate. See the Partner section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 6, 'instruction', 1, 'Partner default flat fee to be paid as commission. See the Partner section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 7, 'instruction', 1, 'Set to ''1'' to enable one-time payment for partners. If set, partners will only get paid once per customer. See the Partner section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 8, 'instruction', 1, 'Partner default payout period unit. See the Partner section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 9, 'instruction', 1, 'Partner default payout period value. See the Partner section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 10, 'instruction', 1, 'Set to ''1'' to enable batch payment payouts using the billing process and the configured payment processor. See the Partner section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 11, 'instruction', 1, 'Partner default assigned clerk id. See the Partner section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 12, 'instruction', 1, 'Currency ID to use when paying partners. See the Partner section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 13, 'instruction', 1, 'Set to ''1'' to e-mail invoices as the billing company. ''0'' to deliver invoices as jBilling.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 14, 'instruction', 1, 'Set to ''1'' to show notes in invoices, ''0'' to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 15, 'instruction', 1, 'Days before the orders ''active until'' date to send the 1st notification. Leave blank to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 16, 'instruction', 1, 'Days before the orders ''active until'' date to send the 2nd notification. Leave blank to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 17, 'instruction', 1, 'Days before the orders ''active until'' date to send the 3rd notification. Leave blank to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 18, 'instruction', 1, 'Prefix value for generated invoice public numbers.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 19, 'instruction', 1, 'The current value for generated invoice public numbers. New invoices will be assigned a public number by incrementing this value.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 20, 'instruction', 1, 'Set to ''1'' to allow invoices to be deleted, ''0'' to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 21, 'instruction', 1, 'Set to ''1'' to allow invoice reminder notifications, ''0'' to disable.');

-- no instructions for 22 - Number of days between invoice generation and first reminder
-- no instructions for 23 - Number of days between subsequent invoice reminders

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 24, 'instruction', 1, 'Set to ''1'' to enable, ''0'' to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 25, 'instruction', 1, 'Set to ''1'' to enable the billing process to calculate interest on overdue payments, ''0'' to disable. Calculation of interest is handled by the selected penalty plug-in.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 27, 'instruction', 1, 'Set to ''1'' to use the "OrderFilterAnticipateTask" to invoice a number of months in advance, ''0'' to disable. Plug-in must be configured separately.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 28, 'instruction', 1, 'PayPal account name.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 29, 'instruction', 1, 'A URL where the graphic of the PayPal button resides. The button is displayed to customers when they are making a payment. The default is usually the best option, except when another language is needed.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 30, 'instruction', 1, 'URL for the HTTP Callback to invoke when the ageing process changes a status of a user.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 31, 'instruction', 1, 'Default "2000-01-01". If this preference is used, the system will make sure that all your invoices have their dates in a incremental way. Any invoice with a greater ''ID'' will also have a greater (or equal) date. In other words, a new invoice can not have an earlier date than an existing (older) invoice. To use this preference, set it as a string with the date where to start.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 32, 'instruction', 1, 'Set to ''1'' to attach a PDF version of the invoice to all invoice notification e-mails. ''0'' to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 33, 'instruction', 1, 'Set to ''1'' to show the "include in separate invoice" flag on an order. ''0'' to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 35, 'instruction', 1, 'Set to ''1'' to include the ID of the order in the description text of the resulting invoice line. ''0'' to disable. This can help to easily track which exact orders is responsible for a line in an invoice, considering that many orders can be included in a single invoice.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 36, 'instruction', 1, 'Set to ''1'' to allow customers to edit their own contact information. ''0'' to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 37, 'instruction', 1, 'Set to ''1'' to mask all credit card numbers. ''0'' to disable. When set, numbers are masked to all users, even administrators, and in all log files.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 38, 'instruction', 1, 'Set to ''1'' to change the subscription status of a user when the user ages. ''0'' to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 39, 'instruction', 1, 'The number of retries to allow before locking the user account. A locked user account will have their password changed to the value of lockout_password in the jbilling.properties configuration file.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 40, 'instruction', 1, 'If greater than zero, it represents the number of days that a password is valid. After those days, the password is expired and the user is forced to change it.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 41, 'instruction', 1, 'Set to ''1'' to allow the usage of the ''main subscription'' flag for orders This flag is read only by the mediation process when determining where to place charges coming from external events.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 42, 'instruction', 1, 'Set to ''1'' to allow the use of pro-rating to invoice fractions of a period. Shows the ''cycle'' attribute of an order. Note that you need to configure the corresponding plug-ins for this feature to be fully functional.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 43, 'instruction', 1, 'If the payment blacklist feature is used, this is set to the id of the configuration of the PaymentFilterTask plug-in. See the Blacklist section of the documentation.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 44, 'instruction', 1, 'Set to ''1'' to allow negative payments. ''0'' to disable');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 45, 'instruction', 1, 'Set to ''1'' to delay payment of negative invoice amounts, causing the balance to be carried over to the next invoice. Invoices that have had negative balances from other invoices transferred to them are allowed to immediately make a negative payment (credit) if needed. ''0'' to disable. Preference 44 & 46 are usually also enabled.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 46, 'instruction', 1, 'Set to ''1'' to allow invoices with negative balances to generate a new invoice that isn''t composed of any orders so that their balances will always get carried over to a new invoice for the credit to take place. ''0'' to disable. Preference 44 & 45 are usually also enabled.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 47, 'instruction', 1, 'ID of the last record read by the mediation process. This is used to determine what records are "new" and need to be read.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 48, 'instruction', 1, 'Set to ''1'' to allow the use of provisioning. ''0'' to disable.');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content)
values (50, 49, 'instruction', 1, 'The threshold value for automatic payments. Pre-paid users with an automatic recharge value set will generate an automatic payment whenever the account balance falls below this threshold. Note that you need to configure the AutoRechargeTask plug-in for this feature to be fully functional.');

-- editable currency
alter table currency add column OPTLOCK int;
update currency set OPTLOCK = 0;

insert into jbilling_seqs (name, next_id) values ('currency', 2); -- if does not exist
-- update jbilling_seqs set next_id = (select (max(id)/10)+1 from currency) where name = 'currency'; -- if already exists

-- breadcrumb descriptions
alter table breadcrumb add column description varchar(255);

-- new filter types
alter table filter add column boolean_value boolean;
alter table filter add column decimal_value numeric(22, 10);
alter table filter add column decimal_high_value numeric(22, 10);

-- remove old gui tables
drop table menu_option;
drop table list_field_entity;
drop table list_field;
drop table list_entity;
drop table list;

delete from international_description where table_id = 63; -- menu_option
delete from international_description where table_id = 77; -- list_entity
delete from international_description where table_id = 78; -- list_field_entity

delete from jbilling_table where name in ('list', 'list_entity', 'list_field', 'list_field_entity', 'menu_option');
delete from jbilling_seqs where name in ('list', 'list_entity', 'list_field', 'list_field_entity', 'menu_option');

-- shorter description for carried invoice status
update international_description set content = 'Carried' where table_id = 90 and foreign_id = 3 and psudo_column = 'description' and language_id = 1;

-- ageing plug-ins
insert into pluggable_task_type_category (id, interface_name) values (24, 'com.sapienter.jbilling.server.process.task.IAgeingTask');

insert into pluggable_task_type (id, category_id, class_name, min_parameters) values (87, 24, 'com.sapienter.jbilling.server.process.task.BasicAgeingTask', 0);
insert into pluggable_task_type (id, category_id, class_name, min_parameters) values (88, 22, 'com.sapienter.jbilling.server.process.task.AgeingProcessTask', 0);
insert into pluggable_task_type (id, category_id, class_name, min_parameters) values (89, 24, 'com.sapienter.jbilling.server.process.task.BusinessDayAgeingTask', 0);

insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (23, 24, 'description', 1, 'Ageing for customers with overdue invoices');

insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 87, 'title', 1, 'Basic ageing');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 87, 'description', 1, 'Ages a user based on the number of days that the account is overdue.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 88, 'title', 1, 'Ageing process task');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 88, 'description', 1, 'A scheduled task to execute the Ageing Process.');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 89, 'title', 1, 'Business day ageing');
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (24, 89, 'description', 1, 'Ages a user based on the number of business days (excluding holidays) that the account is overdue.');

-- Date: 25-May-2011
-- Redmine Issue: 996
-- Description: Fixed reference data for error in processing order of Mediation Reader plugins
update pluggable_task set processing_order=2 where id=6020;
update pluggable_task set processing_order=3 where id=480;


-- Date: 03-Jun-2011
-- Redmine Issue: #576
-- Description: User permissions and role screens
-- editable user permissions
insert into jbilling_seqs (name, next_id) values ('permission_user', 1);

-- delete all old permissions and permission types
delete from international_description where table_id = 59;
delete from permission_role_map;
delete from permission_user;
delete from permission;
delete from permission_type;

-- delete obsolete roles
update user_role_map set role_id = 2 where role_id = 1; -- move internal to super user
delete from role where id = 1;

update user_role_map set role_id = 2 where role_id = 3; -- move clerk to super user
delete from role where id = 3;

update user_role_map set role_id = 5 where role_id = 4; -- move partner users to customer
delete from role where id = 4;

-- new permissions
insert into permission_type (id, description) values (1, 'Customer');
insert into permission_type (id, description) values (2, 'Order');
insert into permission_type (id, description) values (3, 'Payment');
insert into permission_type (id, description) values (4, 'Product');
insert into permission_type (id, description) values (5, 'Product Category');
insert into permission_type (id, description) values (6, 'Plan');
insert into permission_type (id, description) values (7, 'Invoice');
insert into permission_type (id, description) values (8, 'Billing');
insert into permission_type (id, description) values (9, 'Menu');
insert into permission_type (id, description) values (10, 'API');

-- customer
insert into permission (id, type_id) values (10, 1);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 10, 'description', 1, 'Create customer');

insert into permission (id, type_id) values (11, 1);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 11, 'description', 1, 'Edit customer');

insert into permission (id, type_id) values (12, 1);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 12, 'description', 1, 'Delete customer');

insert into permission (id, type_id) values (13, 1);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 13, 'description', 1, 'Inspect customer');

insert into permission (id, type_id) values (14, 1);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 14, 'description', 1, 'Blacklist customer');

-- orders
insert into permission (id, type_id) values (20, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 20, 'description', 1, 'Create order');

insert into permission (id, type_id) values (21, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 21, 'description', 1, 'Edit order');

insert into permission (id, type_id) values (22, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 22, 'description', 1, 'Delete order');

insert into permission (id, type_id) values (23, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 23, 'description', 1, 'Generate invoice for order');

-- payments
insert into permission (id, type_id) values (30, 3);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 30, 'description', 1, 'Create payment');

insert into permission (id, type_id) values (31, 3);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 31, 'description', 1, 'Edit payment');

insert into permission (id, type_id) values (32, 3);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 32, 'description', 1, 'Delete payment');

insert into permission (id, type_id) values (33, 3);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 33, 'description', 1, 'Link payment to invoice');

-- products
insert into permission (id, type_id) values (40, 4);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 40, 'description', 1, 'Create product');

insert into permission (id, type_id) values (41, 4);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 41, 'description', 1, 'Edit product');

insert into permission (id, type_id) values (42, 4);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 42, 'description', 1, 'Delete product');

-- product category
insert into permission (id, type_id) values (50, 5);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 50, 'description', 1, 'Create product category');

insert into permission (id, type_id) values (51, 5);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 51, 'description', 1, 'Edit product category');

insert into permission (id, type_id) values (52, 5);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 52, 'description', 1, 'Delete product category');

-- plans
insert into permission (id, type_id) values (60, 6);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 60, 'description', 1, 'Create plan');

insert into permission (id, type_id) values (61, 6);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 61, 'description', 1, 'Edit plan');

insert into permission (id, type_id) values (62, 6);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 62, 'description', 1, 'Delete plan');

-- invoices
insert into permission (id, type_id) values (70, 7);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 70, 'description', 1, 'Delete invoice');

insert into permission (id, type_id) values (71, 7);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 71, 'description', 1, 'Send invoice notification');

-- billing
insert into permission (id, type_id) values (80, 8);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 80, 'description', 1, 'Approve / Disapprove review');

-- menu
insert into permission (id, type_id) values (90, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 90, 'description', 1, 'Show customer menu');

insert into permission (id, type_id) values (91, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 91, 'description', 1, 'Show invoices menu');

insert into permission (id, type_id) values (92, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 92, 'description', 1, 'Show order menu');

insert into permission (id, type_id) values (93, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 93, 'description', 1, 'Show payments &amp; refunds menu');

insert into permission (id, type_id) values (94, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 94, 'description', 1, 'Show billing menu');

insert into permission (id, type_id) values (95, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 95, 'description', 1, 'Show mediation menu');

insert into permission (id, type_id) values (96, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 96, 'description', 1, 'Show reports menu');

insert into permission (id, type_id) values (97, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 97, 'description', 1, 'Show products menu');

insert into permission (id, type_id) values (98, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 98, 'description', 1, 'Show plans menu');

insert into permission (id, type_id) values (99, 9);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 99, 'description', 1, 'Show configuration menu');

-- api
insert into permission (id, type_id) values(120, 10);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 120, 'description', 1, 'Web Service API access');

-- default permissions for super users
insert into permission_role_map (role_id, permission_id) select 2, id from permission; -- all


-- Date: 28-Jun-2011
-- Redmine Issue: #1063
-- Description: Implement Permissions (second pass)

insert into permission (id, type_id) values (15, 1);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 15, 'description', 1, 'View customer details');

insert into permission (id, type_id) values (16, 1);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 16, 'description', 1, 'Download customer CSV');

insert into permission (id, type_id) values (24, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 24, 'description', 1, 'View order details');

insert into permission (id, type_id) values (25, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 25, 'description', 1, 'Download order CSV');

insert into permission (id, type_id) values (34, 3);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 34, 'description', 1, 'View payment details');

insert into permission (id, type_id) values (35, 3);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 35, 'description', 1, 'Download payment CSV');

insert into permission (id, type_id) values (43, 4);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 43, 'description', 1, 'View product details');

insert into permission (id, type_id) values (44, 4);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 44, 'description', 1, 'Download product CSV');

insert into permission (id, type_id) values (63, 6);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 63, 'description', 1, 'View plan details');

insert into permission (id, type_id) values (72, 7);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 72, 'description', 1, 'View invoice details');

insert into permission (id, type_id) values (73, 7);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 73, 'description', 1, 'Download invoice CSV');

-- permissions for super users
insert into permission_role_map (role_id, permission_id) values (2, 15);
insert into permission_role_map (role_id, permission_id) values (2, 16);
insert into permission_role_map (role_id, permission_id) values (2, 24);
insert into permission_role_map (role_id, permission_id) values (2, 25);
insert into permission_role_map (role_id, permission_id) values (2, 34);
insert into permission_role_map (role_id, permission_id) values (2, 35);
insert into permission_role_map (role_id, permission_id) values (2, 43);
insert into permission_role_map (role_id, permission_id) values (2, 44);
insert into permission_role_map (role_id, permission_id) values (2, 63);
insert into permission_role_map (role_id, permission_id) values (2, 72);
insert into permission_role_map (role_id, permission_id) values (2, 73);


-- Date: 04-Jul-2011
-- Redmine Issue: #1083
-- Description: Order builder: make line description and price editable

insert into permission (id, type_id) values (26, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 26, 'description', 1, 'Edit line price');

insert into permission (id, type_id) values (27, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 27, 'description', 1, 'Edit line description');

-- permissions for super users
insert into permission_role_map (role_id, permission_id) values (2, 26);
insert into permission_role_map (role_id, permission_id) values (2, 27);


-- Date: 05-Jul-2011
-- Redmine Issue: #1092
-- Description: Customer list permissions

insert into permission (id, type_id) values (28, 2);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 28, 'description', 1, 'View all customers');

insert into permission (id, type_id) values (36, 3);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 36, 'description', 1, 'View all customers');

insert into permission (id, type_id) values (74, 7);
insert into international_description (table_id, foreign_id, psudo_column, language_id, content) values (59, 74, 'description', 1, 'View all customers');

-- permissions for super users
insert into permission_role_map (role_id, permission_id) values (2, 28);
insert into permission_role_map (role_id, permission_id) values (2, 36);
insert into permission_role_map (role_id, permission_id) values (2, 74);

-- permissions for customers
insert into permission_role_map (role_id, permission_id) values (5, 24);
insert into permission_role_map (role_id, permission_id) values (5, 30);
insert into permission_role_map (role_id, permission_id) values (5, 34);
insert into permission_role_map (role_id, permission_id) values (5, 72);
insert into permission_role_map (role_id, permission_id) values (5, 91);
insert into permission_role_map (role_id, permission_id) values (5, 92);
insert into permission_role_map (role_id, permission_id) values (5, 93);


-- Date: 11-Jul-2011
-- Description: Categories excluded from percentage line calculations

drop table if exists item_type_exclude_map;
create table item_type_exclude_map (
    item_id int NOT NULL,
    type_id int NOT NULL,
    PRIMARY KEY (item_id, type_id)
);

alter table item_type_exclude_map add constraint item_type_exclude_item_id_FK foreign key (item_id) references item (id);
alter table item_type_exclude_map add constraint item_type_exclude_type_id_FK foreign key (type_id) references item_type (id);

-- Date: 13-Jul-2011
-- Description: Item selector price model

alter table order_line add column use_item boolean;
update order_line set use_item = false where use_item is null;
alter table order_line alter column use_item set not null;
