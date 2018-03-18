# AirExpress

A web application of flight search and reservation system.

## Overview

AirExpress flight search and reservation system is a **RESTful** web application, which serves for a demo purpose of simulating the architectures of many existing traverling agencies' website.

The system is built with several functional sections. It includes a web-server implemeted with Node, which takes responsibility for directing HTTP requirements from front-end clients. It also includes a backend Java server, which takes responsibility for the searching and reserving business logics. The system is supported by a SQL database, which stores all the flight infomation. The message flow between the web-server and backend is done throught a remote procedure call, RPC.

Web-server: https://github.com/EasonJackson/AirExpress-Frontend

Backend-server: https://github.com/EasonJackson/AirExpress-Backend

## Prerequisites

- Linux/Ubuntu 16.04 https://nodejs.org/en/download/
- Node.js and Express.js https://www.npmjs.com/
- Java https://java.com/en/download/
- Google Map Developer Tool https://developers.google.com/maps/
- JSON-RPC 2.0 Base http://code.google.com/p/json-smart/

## Installing and Test on Local Env
Download the source code on your local environment, and then navigate to the folder where the source code locates.

**To start a web-server**:
```
$ ./bin/www
```
The server is by default running on ```localhost:3000```

On successful starting, the web-server will start querying airport geometry information with Google Map API. Once done it will show:

```
Airport loaded.
```

A Java PRC client instance is automatically instatiated with the web-server.

**To start a backend server**:

It makes easier to use an IDE to compile the source code, and then start the ```ExampleServer``` under path ```./RPC/src/main/java/```

The backend server is by default running on ```localhost:8080```

On successful starting, the backend server will call an echo method to conduct a self-test.

The following message will be displayed once the echo method is finished:

```
Request:
{"method":"echo", "id":"req-id-01","params":["Self testing query of List Param"], "jsonrpc":"2.0"}
Local Self testing query of Named Param Local Self testing query of Named Param
Test passed
{"result":"Self testing query of List Param","id":"req-id-01","jsonrpc":"2.0"}
RPC request dispatch server is running. Local port: 8080
```

### Example HTTP Request

```
GET: localhost:3000/rawresult?dep_airport=BOS&ari_airport=LAX&dep_time=2017_05_12&ret_time=2017_05_14&searchButton=GO+NOW
```

## Comment
- Update 2017
    - The  remote MySQL database server is shut down.

## Version

* v1.0

## Authors

* **EasonJackson** - *@2017* 