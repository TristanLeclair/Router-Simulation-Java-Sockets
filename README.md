# Simulate Link State Routing Protocol with Java Socket Programming

## Goal

In this project, you are supposed to develop a pure user-space program which simulates the
major functionalities of a routing device running a simplified Link State Routing protocol.

To simulate the real-world network environment, you have to start multiple instances of the
program, each of which connecting with (some of) others via socket. Each program instance
represents a router or host in the simulated network space. Correspondingly, the links
connecting the routers/hosts and the IP addresses identifying the routers/hosts are simulated
by the in-memory data structures.

By defining the format of the messages transmitting between the program instances, as well as
the parser and the handlers of these messages, you simulate the routing protocol with the user-
space processes.

## Prerequisite

Before you start this project, please ensure that you understand the basic concept of routing, especially Link State Routing, which is taught in class.

### Socket Programming 101

Socket is the interface between the application layer and transmission layer

![Graph of socket programming](./docs/assets/socket_description.png)

The existence of Socket interface greatly reduces the complexity of developing network-based
applications. When the processes communicate via socket, they usually play two categories of
roles, `server` and `client`. The usages of socket in these two roles are different.

![Server vs Client capabilities](./docs/assets/Pasted image 20240405162215.png)

In `server` side, the program creates a socket instance by calling `socket()`. With this socket instance, you can `bind()` it to a specific IP address and port, call `listen()` to wait for the connecting requests, `accept()` to accept the connection. After you call `ACCEPT()`, you can transmit data with the client by calling and `recv()` and `send()`. After you finish all tasks in server side, you can call `close()` to shut down the socket.

In `client` side, the story seems a bit simpler, after you call `socket()` to create a socket instance, you only need to call `connect()` with the specified IP address and port number to request the service from the server side. After the connection is established, the following process is very similar to server side, i.e. transmit data with `recv()` and `send()`, and shut down with `close()`. 

This is the general process of the socket-based network communication. To understand it better, you are suggested to read the article in [here](http://gnosis.cx/publish/programming/sockets.html). The article is described in C programming language, which exposes many details of network data transmission but helpful to understand the concepts.

## Java Socket Programming

Different programming languages offer their own abstractions over socket interface to help the user to develop network-based programs. You are requestd to finish this project in Java. Java provides higher level abstraction for socket than C. In server side, You only need to call `ServerSocket serverSocket = new ServerSocket(port);` to create socket, bind, listen in one shot. In client side, `Socket client = new Socket(serverName, port);` to create socket instance and connect to the remote server.
