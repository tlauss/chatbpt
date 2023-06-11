$schema = @'
create schema if not exists ChatBptDb;
use ChatBptDb;

drop table if exists BannedUser_Chatroom;
drop table if exists User_Chatroom;
drop table if exists Message;
drop table if exists Chatroom;
drop table if exists User;

create table User (uid int primary key auto_increment,
                   name varchar(255) NOT NULL,
                   password varchar(255) NOT NULL,
                   shortname varchar(255) NOT NULL
                   );

create table Chatroom (name varchar(255) primary key,
                       owner_id int NOT NULL,
                       foreign key (owner_id) references User(uid)
                       );

create table BannedUser_Chatroom (user_id int,
                                  chatroom_name varchar(255),
                                  primary key (user_id, chatroom_name),
                                  foreign key (user_id) references User(uid),
                                  foreign key (chatroom_name) references Chatroom(name)
                                  );

create table User_Chatroom (user_id int,
                            chatroom_name varchar(255),
                            primary key (user_id, chatroom_name),
                            foreign key (user_id) references User(uid),
                            foreign key (chatroom_name) references Chatroom(name)
                            );

create table Message (mid int auto_increment primary key,
                      user_id int NOT NULL,
                      text varchar(255) NOT NULL,
                      timestamp datetime NOT NULL,
                      chatroom_name varchar(255) NOT NULL,
                      foreign key (user_id) references User(uid),
                      foreign key (chatroom_name) references Chatroom(name)
                      );
'@

echo $schema | docker exec -i mysql mysql
