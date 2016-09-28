create table directories (
	id integer primary key AUTO_INCREMENT,
	name varchar(50) default '' unique
);

create table emails (
	id integer primary key AUTO_INCREMENT,
	msgNumber integer default 0,
	rcvDate timestamp,
	directory integer,
	bcc varchar(255),
	cc varchar(255),
	fromEmail varchar(50),
	message text,
	toEmails varchar(255),
	replyTo varchar(255),
	sentDate timestamp,
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
