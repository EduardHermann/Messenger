# Messenger

## Description

- With the use of this messenger several people are able to send messages to each other.
- Whenever a client connects to the server the latest data is getting read in by the server from the database and is getting send to all currently connected clients in order to make them able to look up the latest messages.
- The user is able to send a message to all clients which are connected to the server by passing in a name and the content of the message.
- In addition to that the server sends the message to all clients with the current date.
- There are two messengers located in this repository "MessengerServer" is just a simple blueprint of a server and a client which are connected to each other without the named database. "MessengerSeverDatabase" is the equivalent to "MessengerServer" but with an "added database".

## How to start the program

- Somebody needs to host the server and needs to pass in his ip address and a port in the client socket object.
- Besides that a database needs to be created with the attached ".sql" files.
- Make sure that the server host and the client users are turning off their unknown network blocking programs.
- Besides that the host needs to give the users the attached jar file in order to make them able to join the server.
- The user needs to double click on the "MessengerServer.jar"/"MessengerServerDatabase.jar" file whenever the host is hosting the server in order to be able to send messages.
- !!!Attention, you need at least Java 1.8/1.15 and windows installed on your pc in order to correctly start my program!!!

## What I have learned from this project

- Communication between a client and a server
- Servers in general
- SQL
- Handling databases
