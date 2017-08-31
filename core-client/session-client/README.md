Cyc Session API
===============

A Java API for managing configurations and connections to Cyc servers.

The Session API defines the basic functionality for connecting to a Cyc server, 
and is used for this purpose by all the other Cyc APIs. The Session API replaces
the need to directly manage the configuration, creation, and caching of
CycAccess objects that was common in the (now-deprecated) OpenCyc API.

For more information, visit the [Cyc Developer Center](http://dev.cyc.com/).

Requirements and Getting Started
--------------------------------

This project is one of several APIs which are intended to be built in 
conjunction. See the [CycCoreAPI](https://github.com/cycorp/CycCoreAPI) project 
to get started.

**Note:** It is _strongly recommended_ to only run the API test suites against a 
_fresh Cyc server instance_ dedicated to that purpose. The API tests may alter
a Cyc server's KB contents.

Installing the CycCoreAPI will install this library, but once you have done so,
you may rebuild this library independently. From the root of this library's
directory, run:

    mvn install

This will run the test suite, which will pop up a GUI panel asking for a Cyc 
server address. If you are running in a headless environment, or wish to 
specify the server at the command line, use the following instead:

    mvn install -Dcyc.session.server=[HOST_NAME]:[BASE_PORT]

For example:

    mvn install -Dcyc.session.server=localhost:3600

Alternately, if you wish to install without running unit tests:

    mvn install -DskipTests=true 

Further Documentation
---------------------

For the latest API documentation and news, or to ask questions or report issues,
visit the [Cyc Developer Center](http://dev.cyc.com/). Code samples may be
downloaded from the [CoreAPIUseCases](https://github.com/cycorp/CoreAPIUseCases)
project.

Contact
-------

For questions about the APIs or issues with using them, please visit the
[Cyc Dev Center issues page](http://dev.cyc.com/issues/).
