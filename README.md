# Distributed-Scrabble-Game
A java application that implements a distributed scrabble game.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.
### Introduction

The breif introduction of the project.

```
This application uses basic client-server architecture. 
The client is responsible for presenting the graphical user interface and handle user inputs. 
The server needs to maintain all the states for clients and games and keep all the clients synchronized.

The final product contains following features:
• The players are playing on a 20 by 20 grid
• Minimum 2 players
• Players place letters on grid in turns
• The user playing the turn highlight the word and let others vote • The view is synchronized between all the players
• GUI for client
• Logout
• User should be able to see other players • The system can host one or more games • Invite user to join the game
• Advanced features are welcome


There are two threads on the client side, one for communication and the other for rendering. 
The server uses one thread per connection therefore higher concurrency can be handled.

The communication between client and server is implemented using socket, which is built on the top of TCP protocol,
so that reliable data transmission can be guaranteed.
The data sent via the sockets are simply json strings. 
The client generates a json object based on the user operation and converts it to string.
When the server receives the request, it simply parse it and extract the method field, 
and invoke corresponding methods with parameters sent by the client.

```

## To run it

* By GUI: first run "simpleServer.java" to run the server.
		Then run "LoginGUI" to run the client.
		
* By jar: It has two jar files- "server.jar" and the server will run in background.
	    Then run the "client.jar" and enter IP address which is 'localhost' and any port number 
      to enter the game.
      
      
## Screenshots

 <img width="868" alt="screen shot 2019-03-08 at 3 05 04 pm" src="https://user-images.githubusercontent.com/40975373/54007371-7dfb0680-41b5-11e9-9aa0-daac32e3eec3.png">


 <img width="868" alt="screen shot 2019-03-08 at 3 05 22 pm" src="https://user-images.githubusercontent.com/40975373/54007374-83f0e780-41b5-11e9-8e6d-9329e0e946ab.png">


## Authors

* **Yiming Zhang** 

## License

This project is licensed under the MIT License.

