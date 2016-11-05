-- dropping
drop table if exists email_address;
drop table if exists attachments;
drop table if exists emails;
drop table if exists addresses;
drop table if exists directories;

-- creating

create table directories (
	id integer primary key AUTO_INCREMENT,
	name varchar(50) default '' unique
);

create table addresses (
    id integer primary key AUTO_INCREMENT,
    address varchar(150) not null
);

create table emails (
	id integer primary key AUTO_INCREMENT,
	msgNumber integer default 0,
	rcvDate timestamp null,
	directory integer,
	fromEmail integer,
	message text,
	sentDate timestamp null,
	subject varchar(50) default '',
	foreign key (directory) references directories(id) ON DELETE CASCADE ON UPDATE CASCADE,
    foreign key (fromEmail) references addresses(id)
);


-- type:
-- 1 - bcc, 2 - cc, 3 - to, 4 - replyTo
create table email_address (
    emailid integer,
    addressid integer,
    address_type integer,
    primary key(addressid, emailid, address_type),
    foreign key (addressid) references addresses(id) ON DELETE CASCADE ON UPDATE CASCADE,
    foreign key (emailid) references emails(id) ON DELETE CASCADE ON UPDATE CASCADE	
);

create table attachments (
    id integer primary key AUTO_INCREMENT,
    binarydata mediumblob not null,
    filename varchar(128) not null default '',
    email integer,
    foreign key (email) references emails(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- inserting

insert into directories (name) values ('inbox');
insert into directories (name) values ('sent');
insert into directories (name) values ('new');
insert into directories (name) values ('drafts');
insert into directories (name) values ('trash');
insert into directories (name) values ('starred');

insert into addresses (address) values ('cs.517.send@gmail.com');
insert into addresses (address) values ('cs.517.receive@gmail.com');
insert into addresses (address) values ('cs.517.send@outlook.com');


insert into emails (msgNumber, rcvDate, directory, fromEmail, message, sentDate, subject) values 
(1, '2015-09-18 01:28:01', 1, 1, 'plain text', null, 'important'),
(2, '2016-09-18 01:28:01', 1, 1, 'plain text2', null, 'important2'),
(3, now(), 1, 1, 'plain text3', null, 'important3'),
(4, null, 2, 2, 'plain text4', '2016-09-18 01:28:01', 'important4'),
(5, null, 5, 2, 'plain text5', '2016-09-18 01:28:01', 'important5'),
(6, null, 2, 2, '<html><body><h2>not plain</h2><img src="cid:c.jpg"/><body></html>', now(), 'important6');


insert into email_address values (1, 2, 3);
insert into email_address values (2, 2, 3);
insert into email_address values (3, 3, 2);
insert into email_address values (3, 1, 2);
insert into email_address values (3, 2, 3);
insert into email_address values (4, 1, 3);
insert into email_address values (5, 1, 3);
insert into email_address values (6, 1, 3);

