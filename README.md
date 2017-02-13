# Sample app to demo JSESSIONID problems on SPA startup

This project is used as a demo project for a javaee war based SPA application using secured endpoints
and some HttpSession caching stuff.

Problematic has been exposed in a [post](http://lists.jboss.org/pipermail/undertow-dev/2017-February/001879.html) to the undertow mailing list ; read more [here](http://lists.jboss.org/pipermail/undertow-dev/2017-February/001879.html).

## Problem description

When the app starts the following occures
- SPA application is accessed on a secured URL (index.html or time.html _see below for differences_)
- once loaded (`$(document).ready()` as demoed using jquery)
    - a first secured business endpoint `/init` is called. This endpoints initializes some data and put them in the HTTP session
    - once this call is successfully performed 20 other business endpoints `/data` are called (imagine some configuration loading or something else) and use the HTTP Session data to perform some logic

Due to some race conditions on client side & to default JSESSIONID handling server side (session id change on login) several business calls to `/data` can fail.

Here is an example of an execution of the SPA at [http://localhost:8080/sessionid/index.html](http://localhost:8080/sessionid/index.html)

> The application logs/shows messages in which `[X::Y]` information correspond to:
>  - _X_ the JSESSIONID received by the server, ie the one sent by the client (or null if none was received) 
>  - _Y_ the JSESSIONID used by the server 

```
init at: /init [null::QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD] application id generated: 3
0 -> /data [QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{0} on app {3}
FAILURE: 3 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
FAILURE: 1 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
FAILURE: 6 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
FAILURE: 2 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
FAILURE: 4 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
FAILURE: 8 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
FAILURE: 5 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
FAILURE: 7 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
10 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{10} on app {3}
FAILURE: 9 :: /data no http session found for requested HTTP session id: QAYLSwZSSu2pDQMaxnkTAkzciteWKKV-3YDDpzpD
13 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{13} on app {3}
11 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{11} on app {3}
14 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{14} on app {3}
12 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{12} on app {3}
16 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{16} on app {3}
18 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{18} on app {3}
19 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{19} on app {3}
17 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{17} on app {3}
15 -> /data [Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7::Kr6hXPeKMXHIHpSJYSHdYxGvhtbjtTbi9yXRWLb7] data initialized for client{15} on app {3}
```

## Scenarios

- [standard SPA](#standard-spa): no modifications the bug occures everytime 
- [stabilized JSESSIONID](#stabilized-jsession-spa): introduction of unecessary sequential businees call before firing multiple asynchronuous data load calls
- [prevent sessionId change SPA](#undertow-servlet-extension-spa): usage of an undertow ServletExtension to prevent new JSESSIONID to be generated  

### standard SPA
 
How to reproduce:
- start wildly: `mvn clean wildfly:run`
- deploy the app: `mvn wildfly:deploy`
- open your browser, clean `localhost` cookies
- navigate to [http://localhost:8080/sessionid/index.html](http://localhost:8080/sessionid/index.html)
    - log into the app using: `user/u` 
- stop wildly: `mvn clean wildfly:shutdown`

Notice all `/data` calls fired asynchronously after the initial call to `/init`.

### stabilized JSESSION SPA

How to reproduce:
- start wildly: `mvn clean wildfly:run`
- deploy the app: `mvn wildfly:deploy`
- open your browser, clean `localhost` cookies
- navigate to [http://localhost:8080/sessionid/time.html](http://localhost:8080/sessionid/time.html)
    - log into the app using: `user/u` 
- stop wildly: `mvn clean wildfly:shutdown`

Notice the sequential call to `/time` endpoint after the initial call to `/init` and before all `/data` asynchronous calls.
This `/time` calls makes the regeneration of the JSESSIONID to occure before the `/data` are fired and thus those calls are performed using a stable JSESSIONID. 

### undertow servlet extension SPA

In this scenario we use an undertow ServletExtension, [DoNotChangeSessionIdOnLogin ](src/main/java/com/agfa/sample/jee/control/undertow/DoNotChangeSessionIdOnLogin.java) to disable the _ChangeSessionIdOnLogin_ functionnality.
 
How to reproduce:
- start wildly: `mvn clean wildfly:run`
- deploy the app with undertow extension: `mvn -Pundertow wildfly:deploy`
- open your browser, clean `localhost` cookies
- navigate to [http://localhost:8080/sessionid-undertow/index.html](http://localhost:8080/sessionid-undertow/index.html)
    - log into the app using: `user/u` 
- stop wildly: `mvn clean wildfly:shutdown`

Notice no rewrite of the JSESSIONID occures as the extension is active.

