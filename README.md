# esbUtils

## Description

esbUtils is a java library to be used by other java applications to communicate 
with the University of Michigan's ESB (API Store). 

It should also work with
other oauth web services.  Pull requests (along with test code) to address 
any issues are welcome.

The code is kept in github and will be public.

* See below for notes on different versions.
* See below for changes that might be useful (TTD).

## Building the project

The application runs on Java 1.7. After checking out the source code use mvn 
to install.

<code>
mvn clean install
</code>

This should produce a jar that can be added to larger applications.  There isn't
a repository for compiled code, so applications may need to make esbUtils 
available initially by compiling it locally.

## Properties File for Testing

There is a test.TEMPLATE file in this project. To test, this file will have to 
be renamed to test.properties. The properties for the file are listed below:

* tokenServer - the URL associated with generating a token for the ESB
* apiPrefix - the URL associated with the specific API that you are using found 
on the overview page of your API in the ESB
* key - Consumer Key found on Subscriptions page in the ESB
* secret - Consumer Secret found on Subscription page in the ESB
* call - A list of available calls is located on the API Console page of 
your API in the ESB
* tokenForceRenewalFrequency - Set this to a non-zero value to force WAPI
to renew the token with that frequency.

Once you have this information it is possible to update the properties file 
with the appropriate information for you to begin testing and verify the 
utility is working.

NOTE: This secure will not be kept in a properties file in this project, 
but held in the properties file of the application that is using the library. 
For example, if Canvas Course Manager was to use this library then in CCM's 
properties file you would find these properties there.

## Testing Application

The class 'RunTest.java' was created for the sole purpose of verifying the 
application will run. To verify:

1. Rename test.TEMPLATE to test.properties
2. Fill in test.properties with accurate information found on the ESB
3. Run class RunTest.java

## Version notes

Version 2.0 - This revision was necessary to deal with a new API manager.  New
headers were required and are explicitly passed from the calling application.

Version 2.1 - This revision adds functionally to explicitly require token 
renewal every *n* requests.  This is essential for testing token renewal.  It 
also makes available *putRequest* and *getRequest* methods that a) can take
optional headers and b) will automatically try to renew an expired token.  
Use of other request methods is discouraged.

## Things to do (TTD)
* Improve test files.
* Depreciate old methods.
* Update java version to 1.8.
* Make several methods protected since they aren't useful outside the 
library. At least the constructors, getRequest, putRequest, and renewToken
methods should remain available.  Methods starting with *do* should be
restricted, depreciated, or removed.
* Extract the Ruby version of this from Dash and make the APIs correspond.

