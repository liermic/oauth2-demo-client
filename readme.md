
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

The OAuth flows of this client are controlled by two controllers. When the home page of the client is called, the `Oauth2ClientUICOntroller` initializes the input form for the user. The form is prepared with the default configuration (see above). The controller is then called again once the user actually starts one of the OAuth flows from the UI. The OAuth flow that is started is specified by a dropdown field in the UI. The controller then performs the following steps:

1. Open an `OauthSession` to hold the input data, result data and a Session-ID. The ID will later be used as client state for implicit and authorization code grant. 

2. Instantiate an `OauthFlowExecution` according to the chosen `OauthFlowType`. 

3. Execute the initial step of given execution instance. 

4. Direct the user agent (browser) to the next page, depending on the chosen flow (see below). This may be the authorization endpoint or the result page of this client. 

The `Oauth2RedirectController` will be called by the authorization server once the user has authorized the requested scopes. This controller has to distinguish between implicit and authorization code grant, see below for details. Either way, the controller will direct the user agent to the result page of that flow.

There is a result page for each of the different flows. The background of those pages is color coded to be **Red**, if an error occurred or the JWT is invalid and **Green** if the token was received and is valid. 


Client Credentials Flow
-----------------------

The client credentials flow is the easiest of the implemented flows. For this flow, the client simply sends a token request to the authorization server. The client authenticates by HTTP Basic and sends a POST request which states the grant type **client_credentials** in its request body. 

```
POST /oauth/myTenantId/myOrganizationId/token HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Authorization: Basic dHJ1c3RlZENsaWVudEtleTp0cnVzdGVkQ2xpZW50U2VjcmV0
Host: localhost:8443

grant_type=client_credentials
```

Using the Nimbus SDK, sending this request is fairly straightforward. Most of the code in the class `ClientCredentialsFlowExecution` deals with extracting data from the user input and populating the result data object. The following code listing assembles and sends the request. 

```Java
private HTTPResponse requestToken(URI uri, String clientID, String clientSecret, Scope scope) 
			throws IOException {
		// prepare client authentication (Http Basic)
		ClientAuthentication auth = new ClientSecretBasic(new ClientID(clientID), new Secret(clientSecret));
		// create request object and transform to HttpRequest
		TokenRequest request = new TokenRequest(uri, auth, new ClientCredentialsGrant(), scope);
		HTTPRequest httpRequest = request.toHTTPRequest();
		// save request for display purposes
		this.result.setTokenRequest(OauthDisplayUtil.prettyPrint(httpRequest));
		// send request and return response
		return httpRequest.send();
	}
```


Implicit Grant Flow
-------------------

Implementing the implicit grant flow has a few pitfalls. The following points will have to be handled differently than the client credentials flow:

* The implicit flow uses the authorization endpoint, but not the token endpoint. 

* Following the request, the client will have to wait for an answer from the authorization server. It has to offer an endpoint for the redirect.

* The JWT is not contained in the message body, but rather as a URL fragment. Those fragments usually remain in the user agent (browser) and are not sent to the server or in this case, to the client application.

Sending the initial request is not really different from the client credentials flow. See the implementing class `ImplicitGrantFlowExecution` for details. You'll notice that this flow sets `session.setNextPage()` to the authorization URL of the authorization server by the keyword `redirect:`. The request also includes the session ID as client state to continue the session later. The following listing shows an example request:

```
GET /oauth/authorize
?response_type=token
&client_id=UdGGXhjZRtWHZimV0TyY6Q
&response_mode=fragment
&state=9665fdbe-6e3e-450b-91bb-4c3cb42f7840
HTTP/1.1
Host: localhost:8443
```

Once the user authorizes the requested scopes, the authorization server will call the redirect endpoint for this client application. The following request will be sent to the `Oauth2RedirectController` (token abbreviated and line breaks added):

```
GET /api/oauth/redirect
#access_token=eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJP
&scope=view-grades+change-address+view-records
&state=9665fdbe-6e3e-450b-91bb-4c3cb42f7840
&token_type=Bearer
&expires_in=600

Host: localhost:8086
```

The state parameter of this request enables the client application to reuse the open session. This sort of session handling may not be necessary for native apps on mobile devices, but it is appropriate for a web application (granted, the authorization code flow is even more appropriate). Since the fragment is not actually passed on to the controller, the client now opens a web page (`decodeFragment.html`) that only includes the following script:

```JavaScript
var fragmentString = location.hash.substr(1);
window.location = "/implicitGrant?" + fragmentString;
```

The script extracts the token from the fragment and calls the controller again, this time with the token data as query component. Calling the controller again may not be necessary in other situations, but the controller will simply decode the token and show the result page.


Authorization Code Grant Flow
-----------------------------

The authorization code grant consists of two requests to the authorization server and one redirect back to the client. The initial authorization request differs from the implicit flow in the parameters `grant_type` and `response_mode`, see the following listing:

```
GET /oauth/authorize
?response_type=token
&client_id=UdGGXhjZRtWHZimV0TyY6Q
&response_mode=code
&state=9665fdbe-6e3e-450b-91bb-4c3cb42f7840
HTTP/1.1
Host: localhost:8443
```

Once the user authorized the requested scopes, the authorization server redirects the user agent (browser) back to the client application. The request includes the authorization code, see this example:

```
GET /api/oauth/redirect
?code=8417-jLXuzvQsmuBCqGonv3azFEwOLIdqlZ_O2NC7IE
&state=1d54785f-a7f2-4496-82f6-48456644a1bb
Host: localhost:8086
```

The code is then used by the `Oauth2RedirectController` to send an additional token request:

```
POST /oauth/myTenantId/myOrganizationId/token HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Host: localhost:8443

grant_type=authorization_code
&code=8417-jLXuzvQsmuBCqGonv3azFEwOLIdqlZ_O2NC7IE
```



Global and Local Scopes
=======================

The implemented authorization server supports both global scopes and local scopes. A **global scope** may be valid for any number of resource servers. An authorization or token request to the default endpoints (see above) may only yield authorization for these global scopes.   

It may be necessary to request authorization for a resource server that only supports local scopes. A **local scope** is not shared between resource servers and is bounded specifically to its resource server. That means that any JWT may contain either global scopes or local scope of a specific resource server, but not both. An authorization request for such a resource server still complies to the OAuth standard by only requiring a different URL. See the following example requests for an authorization code grant:
 
```
GET /oauth/authorize/localScopedResourceServer
?response_type=code
&client_id=UdGGXhjZRtWHZimV0TyY6Q
&response_mode=query
HTTP/1.1
Host: localhost:8443
```

The following listing concludes by sending the token request to a corresponding token endpoint:

```
POST /oauth/myTenantId/myOrganizationId/token/localScopedResourceServer
HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Host: localhost:8443

grant_type=authorization_code
&code=8417-jLXuzvQsmuBCqGonv3azFEwOLIdqlZ_O2NC7IE
```



