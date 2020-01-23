# WORK IN PROGRESS

This project is not yet functional; please check back later.

# Fortify SSC Bug Tracker Plugin for ALM Octane

This project provides an SSC Bug Tracker Plugin implementation for submitting vulnerabilities 
from Fortify SSC to ALM Octane. 

## Building & installing the plugin

To build this project, run `gradlew clean build`. This will generate the plugin jar in the `build`
directory; this plugin jar can be uploaded to SSC under SSC Administration->Plugins->Bug Tracking.

The project includes a number of JUnit tests. Some of these tests may run as-is, other tests may require
a connection to ALM Octane. These tests will be skipped if no connection to ALM Octane has been configured.
The ALM Octane connection can be configured by passing the following property definitions when running
a Gradle build:

`gradlew clean build -DoctaneUrl=http://host:port/ -DoctaneSharedSpaceId=1001 -DoctaneWorkspaceId=1002 -DoctaneUserName=myuser -DoctanePassword=mypassword`

Once the plugin has been installed in SSC for the first time, you will need to add the following 
configuration under SSC Administration->Templates->Bug Filing:

* Bug Tracker Plugin: ALM Octane
* Description: Templates for ALM Octane text fields
* Mapped Fields:
    * Description: For now, copy from the Description field for the JIRA template
    * Name: For now, copy from the Summary field for the JIRA template
    
![Screenshot](https://github.com/fortify-ps/BugTrackerPluginALMOctane/raw/master/SSC-templatesbugfields.png "Screenshot")


## Comparison with other SSC bug tracker plugins

This plugin was developed at the request of Fortify Product Management, and as such is a candidate for
being bundled with a future SSC version. With this in mind, this plugin tries to follow the structure
and principles of other SSC bug tracker plugins, mostly using the SSC JIRA bug tracker plugin sample as 
a basis for this ALM Octane bug tracker plugin 

This section provides a detailed overview of both similarities and differences between this plugin and 
the plugins currently bundled with SSC.

* Gradle build:
    * The `build.gradle` file is very similar to other bug tracker plugins,
      with all dependencies loaded from the lib folder. The main difference
      is the addition of JUnit-related repositories, dependencies and 
      configuration, in order to enable the use of JUnit tests.
    * This `build.gradle` file requires Gradle 4.x, whereas most people will
      likely have already upgraded to Gradle 5.x or 6.x. To allow this project
      to be built without having to install an old Gradle version, Gradle 
      Wrapper scripts were added to this project.
* lib-folder:
    * Removed Jettison dependencies
    * Added JSON-P dependencies; compared to Jettison, JSON-P provides
      nice builder patterns and doesn't throw JSONExceptions everywhere
    * Added commons-lang3-3.9 dependency, as it provides more features than
      the already included commons-lang-2.6 dependency
* Source code:
    * Much of the non-JIRA-specific code from the SSC JIRA plugin has been re-used
      in this ALM Octane plugin, like handling connections to the target system, using
      enum's to generate configuration options, ... 
    * However, the source code structure has been largely refactored in order to 
      provide better encapsulation and separation of concerns. This results in a lot
      more classes, with each class focusing on just a single task/responsibility.  
      