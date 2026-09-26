# UDP Multi-Client Network Project

Java networking project implementing a UDP server capable of handling multiple clients concurrently.

## Features

- UDP client/server communication
- Concurrent request handling through dedicated communication and reception threads
- Client information tracking
- Source-code documentation with Javadoc

## Project files

- `UDPServerMulti.java`: multi-client UDP server
- `UDPClientMulti.java`: client implementation
- `CommunicationThread.java` and `ReceptionThread.java`: concurrent communication handling
- `ClientInfo.java`: client data model
- `docs/`: generated or supporting documentation

## Running locally

Compile the Java sources, start `UDPServerMulti`, then run one or more `UDPClientMulti` instances in separate terminals or IDE sessions.

## Scope

Academic project demonstrating UDP communication, multi-client coordination and Java concurrency.
