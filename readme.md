
Overview
========

This project contains an example client application for OAuth 2.0 protocol flows. 
It was implemented in support of a master thesis which implemented an OAuth 2.0 Authorization 
Server with

* Support for authorization code grant
* Support for implicit grant
* Support for client credentials grant
* Access token as JWT
* Authorization through Structure and Agency Policy Language (SAPL)
* Global and local Scopes


Features
--------

This project is based on Spring Boot and will boot a web application on start. 
The web application approach is used for several reasons:

* the input parameters can be changed through a web interface.
* the client needs to offer an URL that is accessible from the authorization server.
* the received access token will be displayed on a result page.  

The project uses Thymeleaf to implement the web interface and Spring-MVC to offer 
an endpoint for the redirect after authorization. The OAuth requests are assembled 
and parsed by the Nimbus SDK framework. If a valid token is returned, the signature 
is also verified.


External Links
--------------

For the OAuth 2.0 specification document, see <https://tools.ietf.org/html/rfc6749>

For Spring Boot, the server infrastructure used by this project, see <https://spring.io/projects/spring-boot>

For Thymeleaf, the framework used for web interfaces in this project, see <https://www.thymeleaf.org/>

For Nimbus SDK, an OAuth implementation library used in this project, see <https://connect2id.com/products/nimbus-oauth-openid-connect-sdk>

For Nimbus JOSE + JWT, an JWT library used in this project, see
<https://connect2id.com/products/nimbus-jose-jwt>


Pre-Requisites
=============

The client is designed to work with any standard compliant authorization server that uses JSON Web Token (JWT) as access tokens. In order to achieve this, all variable data of the authorization server is configured in the **application.yml**. Foremost hat includes the authorization URL, the token URL and the client credentials (key, secret). It also contains properties for retrieval of JSON Web Keys (JWK) and the expected signature algorithm of received tokens. All of these properties serve as default and can be changed in the web interface to allow for easy testing. 

The application runs on **port 8086** by default. The redirect URL that needs to be registered at the authorization server is **http://localhost:8086/api/oauth/redirect** for a local test installation. 



OAuth 2.0 Flows
===============

Flow Control and Data Model
---------------------------



Client Credentials Flow
-----------------------


Implicit Grant Flow
-------------------


Authorization Code Grant Flow
-----------------------------


 