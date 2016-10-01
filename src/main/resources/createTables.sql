-- creating new tables
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
	rcvDate timestamp,
	directory integer,
	fromEmail integer,
	message text,
	sentDate timestamp,
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
