**Table of Contents**  *generated with [DocToc](http://doctoc.herokuapp.com/)*

- [Introduction](#introduction)
- [Pre-requisites](#pre-requisites)
- [Application Domain](#application-domain)
- [Analytics Demo](#analytics-demo)
	- [Running Demo](#running-demo)
		- [Possible issues](#possible-issues)
- [Real-Time Demo](#real-time-demo)
	- [Running Demo](#running-demo-1)
- [Reference](#reference)
	- [Infinispan Server Docker Image](#infinispan-server-docker-image)
- [Live Events](#live-events)
- [Blogs](#blogs)


# Introduction 

This is a repository contaning example applications/demos based on the Swiss transport data set. 
It shows how to use Infinispan for real-time and offline analytics use cases.


# Pre-requisites

* OpenShift Origin 1.4.1
* Docker 1.13.1
* Maven 3
* Install Jupyter via [Anaconda](https://www.continuum.io/downloads).
It is recommended that the Python 3.x version is installed.

These demos could be run alternatively using [Minishift](https://www.openshift.org/minishift/).


# Application Domain

The demos presented in this repository use rail-based transport as theme.
The domain is constructed out of the following entities:

* `Train` - a train that transport passengers around the country, it contains:
  * A name, e.g. `ICN 531`)
  * A destination, e.g. `Basel SBB`
  * Category, e.g. `ICE` for intercity europe trains
  * An identifier.
  The identifier is a concatenation of the train name, destination and departure time.

* `Station` - represents a physical train station, it contains:
  * A numeric identifier, e.g. `8501008`
  * A name (e.g. `Basel SBB`)
  * A geographic location including latitude and longitude information. 

* `Stop` - represents a train's passing through a station.
It represents each of the entries in a given station's train board.
It contains:
  * Train information, as described above.
  * The expected departure timestamp for a given train through a station.
  * The platform where the train is expected to stop.
  * If train is delayed, the number of minutes that a train is delayed going through the station.

* `StationBoard` - represents the list of train stops through a station at a given time, contains:
  * Station board entries represented as upcoming trains to stop in this station.
  * Time when the station board information was collected.


# Analytics Demo

The aim of the analytics demo is to show how to use Infinispan extensions to Java Streams to run distributed computations.
One of the new features introduced in Infinispan 8 was distributed streams, which enhances Java Stream capabilities to run on a distributed cache.
The key aspect of these enhancements is that instead of moving around data to do any processing, Infinispan moves the functions or lambdas around the cluster so that they are executed against local data.

This is a very powerful feature since it enables Java developers to use familiar Java Streams API to do distributed computing.
This demo shows how Infinispan distributed streams can be used against remotely against cluster of Infinispan servers.

The final objective of the demo is to use historic data of train station board information to answer this question: 

> What is the time of the day when there is the biggest ratio of delayed trains?

To help answer this question, a remote cache is defined to contain historic information of all train stops keyed by train id:  

```java
RemoteCache<String, Stop>
```
    
*Note: A train goes through multiple stations and hence it has multiple stops.
For simplicity, only the last stop in terms of time is kept*

This demo uses protocol buffers to describe the types involved in the demo so that they can be unmarshalled remotely in the server.
This is necessary so that the remote server task can work with user types as opposed to binary data.

This means that when the analytics application starts, there are a few set up invocations:

* For each type stored, declare it as a protocol buffers message type in a `.proto` file.
* Store the `.proto` file in the Infinispan Server's protobuf metadata cache.
* Store `.proto` file and a marshaller for each of the message types in the client. 
* Store proto marshallers in server via the execution of a remote task.

Once the set up is complete, the `InjectorVerticle` in `analytics-client` project will store 3 week's worth of data from station boards.
After data loading has completed, the delay ratio task can be executed via HTTP which returns the result of the distributed analytics computation to the client.
Then, the result can be consumed by a Jupyter notebook to provide a plot that answers the question posed above.

Below are more detailed instructions on how to run the demo.


## Running Demo

Start OpenShift cluster:

```bash
oc cluster up --public-hostname=127.0.0.1
```
    
Next, deploy all the applications required by the demo:

```bash
./deploy-all.sh
```

Open browser (preferably Chrome) and check the [console](https://127.0.0.1:8443/console/project/myproject/overview) to see if pods are up.
There are alternative ways to verify the status of applications on OpenShift.
Check the OpenShift documentation for more details.

Next, open the Jupyter notebook for this demo:

```bash
cd analytics/analytics-jupyter
~/anaconda/bin/jupyter notebook
```

Open `delayed-trains-ratio.ipynb` notebook and verify that each cell calculates without an error. 
The result should show that 2am is the time of the day when there is the biggest ratio of delayed trains.

### Possible issues

While running the demo, you might find that the application route server host is not available.
If that's the case, switch from `xip.io` to `nip.io`, or use a hosts file manager like [Gas Mask](https://github.com/2ndalpha/gasmask)(thx Clement for tip!).


# Real-Time Demo

The aim of the real-time demo is to show how to Infinispan's Continuous Query technology can be used to track changing data.
Initially, continuous Query involves defining an query and a listener implementation.
When the query is executed, any matching data gets passed in to the listener implementation as part of the joining result set.
As more data is added or removed, the listener gets invoked with any new matches, or matches that are no longer part of the result set.

For this demo, a remote cache is defined as: 

```java
RemoteCache<Station, StationBoard> stationBoards...
```

The final objective of the demo is to present a live-updating table of delayed trains.
To help achieve this objective, a remote cache should be populated with each station's upcoming train board information at a given time.

Remote querying uses protocol buffers as common format for being able to deconstruct binary data.
So, once the remote cache has been defined, the following steps are required before the query can be defined:

* For each type stored, declare it as a protocol buffers message type in a `.proto` file.
* Store the `.proto` file in the Infinispan Server's protobuf metadata cache.
* Store `.proto` file and a marshaller for each of the message types in the client. 
 
Next, a query is defined as matching any station boards where at least one of the train stops is delayed:

```java
QueryFactory qf = Search.getQueryFactory(stationBoards);
Query query = qf.from(StationBoard.class)
   .having("entries.delayMin").gt(0L)
   .build();
```

Once the query is defined, a continuous query listener is attached to it:

```java
ContinuousQueryListener<Station, StationBoard> listener = 
         new ContinuousQueryListener<Station, StationBoard>() {
   @Override
   public void resultJoining(Station key, StationBoard value) {
      // ...
   }

   @Override
   public void resultUpdated(Station key, StationBoard value) {
      // ...
   }

   @Override
   public void resultLeaving(Station key) {
      // ... 
   }
};
```

```java
continuousQuery = Search.getContinuousQuery(stationBoards);
continuousQuery.addContinuousQueryListener(query, listener);
```

When the demo application runs, it cycles through some cached station board data and injects that information to the remote cache.
As data gets updated and delayed station board entries are found, these are sent to the Vert.x event bus.
The event bus in turn pushes those events via a SockJS bridge which can be consumed by a Vert.x Websocket client endpoint.
The demo contains a JavaFx application for showing the delayed trains dashboard which uses the Vert.x Websocket client API.

## Running Demo

If you haven't already, start the OpenShift cluster and deploy all the applications:

```bash
oc cluster up --public-hostname=127.0.0.1
./deploy-all.sh 
```

Next, execute `delays.query.continuous.fx.FxApp` application from the IDE or the command line.


# Reference

## Infinispan Server Docker Image

The demo currently uses a custom Infinispan Server 9.0.x [docker image](https://hub.docker.com/r/galderz/infinispan-server).
To be more specific, it contains this [commit](https://github.com/galderz/infinispan/commit/1e092e9e993c784a80e2b043fc6d2a4b3b07a822) which does a couple of things:

* Disables protected cache write security checks. The reasons for doing that are explained in this [developer mailing list discussion](http://lists.jboss.org/pipermail/infinispan-dev/2017-April/017439.html).
This is just a temporary measure until default secured configuration works without hitches.
* Adds a module dependency to the server distribution so that `org.infinispan.query.remote.CompatibilityProtoStreamMarshaller` can be set as compatibility mode marshaller.
More details can be found in [ISPN-7711](https://issues.jboss.org/browse/ISPN-7711).


# Live Events
 
Here's a list of conferences and user groups where this demo has been presented.
The `live-events` folder contains step-by-step instructions of the demos, as presented in these live events:

* 27th April 2017 - Great Indian Developer Summit 2017
(
[slides](https://speakerdeck.com/galderz/big-data-in-action-with-infinispan)
|
video NA
|
[live demo steps](live-events/gids17.md)
)
* 13th June 2017 - Berlin Buzzwords 2017
(
[slides](https://speakerdeck.com/galderz/big-data-in-action-1)
|
[video](https://www.youtube.com/watch?v=Q0AaSKdhhwg)
|
[live demo steps](live-events/bbuzz17.md)
)
* 7th September 2017 - DevNation Live 2017
(
[slides](https://speakerdeck.com/galderz/big-data-in-action-with-infinispan-2)
|
[video](https://www.youtube.com/watch?v=ZUZeAfdmeX0)
|
[live demo steps](live-events/devnation17.md)
)
* 23rd November 2017 - Madrid Java User Group 2017
(
[slides](https://speakerdeck.com/galderz/data-grids-descubre-que-esconden-los-datos)
|
video NA
|
[live demo steps](live-events/madridjug17.md)
)


# Blogs

Here's a list of blog posts where this demo has been featured:

* 6th July 2017 - [Reactive Big Data demo working with Infinispan 9.0.3.Final](http://blog.infinispan.org/2017/07/reactive-big-data-demo-working-with.html)
* 5th May 2017 -
[Reactive Big Data on OpenShift In-Memory Data Grids](http://blog.infinispan.org/2017/05/reactive-big-data-on-openshift-in.html) 
