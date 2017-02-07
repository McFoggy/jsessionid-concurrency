# Sample app to demo JSESSIONID problems on SPA startup

This project is used as a demo project for a javaee war based SPA application using secured endpoints
and some HttpSession caching stuff.

## Problem description

When the app starts the following occures
- SPA application is accessed on a secured URL (index.html or time.html _see below for differences_)
- once loaded (`$(document).ready()` as demoed using jquery)
    - a first secured business endpoint `/init` is called. This endpoints initializes some data and put them in the HTTP session
    - once this call is successfully performed 20 other business endpoints `/data` are called (imagine some configuration loading or something else) and use the HTTP Session data to perform some logic

Due to some race conditions on client side & to default JSESSIONID handling server side (session id change on login) several business calls to `/data` can fail.

Here is an example of an execution of the SPA at [http://localhost:8443/sessionid/index.html](http://localhost:8443/sessionid/index.html)

```
init at: [null::p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-] application id generated: 2
0 -> [p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-::KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G] for client{0::2}, data initialized
FAILURE: 4 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 3 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 7 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 8 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 11 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 12 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
15 -> [KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G::KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G] for client{15::2}, data initialized
16 -> [KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G::KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G] for client{16::2}, data initialized
19 -> [KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G::KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G] for client{19::2}, data initialized
FAILURE: 1 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
18 -> [KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G::KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G] for client{18::2}, data initialized
FAILURE: 2 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 6 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 9 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 13 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 10 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
FAILURE: 5 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
17 -> [KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G::KHKcbRFB-mQEps0lhPe1EzyVfloUD9PSf7kiGL9G] for client{17::2}, data initialized
FAILURE: 14 :: no http session found for requested HTTP session id: p8Z-QlPSX_VBtwtt_xSWce8jdA_wefidNfpiYG3-
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
- navigate to [http://localhost:8443/sessionid/index.html](http://localhost:8443/sessionid/index.html)
    - log into the app using: `user/u` 
- stop wildly: `mvn clean wildfly:shutdown`

Notice all `/data` calls fired asynchronously after the initial call to `/init`.

### stabilized JSESSION SPA

How to reproduce:
- start wildly: `mvn clean wildfly:run`
- deploy the app: `mvn wildfly:deploy`
- open your browser, clean `localhost` cookies
- navigate to [http://localhost:8443/sessionid/time.html](http://localhost:8443/sessionid/time.html)
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
- navigate to [http://localhost:8443/sessionid-undertow/index.html](http://localhost:8443/sessionid-undertow/index.html)
    - log into the app using: `user/u` 
- stop wildly: `mvn clean wildfly:shutdown`

Notice no rewrite of the JSESSIONID occures as the extension is active.

