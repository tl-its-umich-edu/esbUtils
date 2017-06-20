# esbUtils

## Description

esbUtils is a java library to be used by other java applications to communicate with the University of Michigan's ESB (API Store). 

The code is kept in github and will be public.

Version 2.x will run using the IBM API Manager.

## Building the project

The application runs on Java 1.7. After checking out the source code use mvn to install.

<code>
mvn clean install
</code>

This should produce a jar that can be added to larger applications.

## Properties File for Testing

There is a test.TEMPLATE file in this project. To test, this file will have to be renamed to test.properties. The properties for the file are listed below:

* tokenServer - the URL associated with generating a token for the ESB
* apiPrefix - the URL associated with the specific API that you are using found on the overview page of your API in the ESB
* key - Consumer Key found on Subscriptions page in the ESB
* secret - Consumer Secret found on Subscription page in the ESB
* call - A list of available calls is located on the API Console page of your API in the ESB

Once you have this information it is possible to update the properties file with the appropriate information for you to begin testing and verify the utility is working.

NOTE: This information will not be kept in a properties file in this project, but held in the properties file of the application that is using the library. For example, if Canvas Course Manager was to use this library then in CCM's properties file you would find these properties there.

## Testing Application

The class 'RunTest.java' was created for the sole purpose of verifying the application will run. To verify:

1. Rename test.TEMPLATE to test.properties
2. Fill in test.properties with accurate information found on the ESB
3. Run class RunTest.java

