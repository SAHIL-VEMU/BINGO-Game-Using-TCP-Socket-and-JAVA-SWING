 # BINGO-Game-Using-TCP-Socket-and-JAVA-SWING

This is a simple BINGO game built using Java Swing and TCP sockets. 

## Prerequisites
* Java Development Kit (JDK) 8 or later
* Apache Maven 3.0 or later

## Setup
1. Clone the repository.
2. Open the project in your preferred IDE (e.g., IntelliJ IDEA, VS Code).

## Running the Game
1. Start the server by compiling & running the `Server.java` class.
```
    javac src/server_files/Server.java
    java src.server_file.Server.java
```
2. Start two clients by compiling & running the `Client.java` class twice, specifying the IP address of the server as a command-line argument. (else default localhost)
```
    javac src/client_files/Client.java
    java src.client_file.Client.java
```

## Gameplay
1. Each player will see a 5x5 grid of numbers.
2. The objective is to be the first player to mark off five numbers in a row, column, or diagonal.
3. Players take turns clicking on numbers on their grid.
4. When a player clicks on a number, that number will be marked off on both players' grids.
5. The first player to mark off five numbers in a row, column, or diagonal wins the game.

## Code Explanation
* The server code is implemented in the `Server.java` class. The server listens for connections from clients and then creates a new thread for each client. Each thread handles the communication with the corresponding client.

* The client code is implemented in the `Client.java` class. The client connects to the server and then sends and receives data from the server. The client also handles the GUI for the game.


