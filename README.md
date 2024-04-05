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
