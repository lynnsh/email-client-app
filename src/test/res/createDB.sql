drop table if exists attachments;
drop table if exists emails;
drop table if exists directories;

create table directories (
	id integer primary key AUTO_INCREMENT,
	name varchar(50) default '' unique
);

create table emails (
	id integer primary key AUTO_INCREMENT,
	msgNumber integer default 0 not null,
	rcvDate timestamp not null,
	directory integer,
	bcc varchar(255) default '',
	cc varchar(255) default '',
	fromEmail varchar(50) default '' not null,
	message text,
	toEmails varchar(255) default '',
	replyTo varchar(255) default '',
	sentDate timestamp not null,
	subject varchar(50) default '',
	foreign key (directory) references directories(id) ON DELETE CASCADE ON UPDATE CASCADE	
);

create table attachments (
    id integer primary key AUTO_INCREMENT,
    binarydata mediumblob not null,
    filename varchar(128) not null default '',
    email integer,
    foreign key (email) references emails(id) ON DELETE CASCADE ON UPDATE CASCADE
);



insert into directories (name) values ('inbox');
insert into directories (name) values ('sent');
insert into directories (name) values ('new');
insert into directories (name) values ('drafts');
insert into directories (name) values ('trash');
insert into directories (name) values ('starred');

insert into emails (msgNumber, rcvDate, directory, bcc, cc, fromEmail, message, toEmails, replyTo, sentDate, subject) values (1, now(), 1, '', '', 'cs.517.send@gmail.com', 'plain text', 'cs.517.receive@gmail.com', '', '2016-09-18 01:28:00', 'important'),
(2, now(), 1, '', '', 'cs.517.send@gmail.com', 'plain text2', 'cs.517.receive@gmail.com', '', '2016-09-18 01:28:01', 'important2'),
(3, now(), 1, '', 'cs.517.send@outlook.com;cs.517.send@gmail.com', 'cs.517.send@gmail.com', 'plain text3', 'cs.517.receive@gmail.com', '', '2016-09-18 01:28:01', 'important3'),
(4, now(), 2, '', '', 'cs.517.receive@gmail.com', 'plain text4', 'cs.517.send@gmail.com', '', '2016-09-18 01:28:01', 'important4'),
(5, now(), 5, '', '', 'cs.517.receive@gmail.com', 'plain text5', 'cs.517.send@gmail.com', '', '2016-09-18 01:28:01', 'important5'),
(6, now(), 2, '', '', 'cs.517.receive@gmail.com', '<html><body><h2>not plain</h2><img src="cid:c.jpg"/><body></html>', 'cs.517.send@gmail.com', '', '2016-09-18 01:28:01', 'important6');



