# Introduction

This project exposes a simple HTTP endpoint exposing a _greeting_ service. The service is available at this address: 
_http://hostname:port/api/greeting_ and returns a JSON response containing the _greeting_ message.

```json
{
    "content": "Hello, World!"
}
```

# Prerequisites

To get started with this quickstart you'll need the following prerequisites:

Name | Description | Version
--- | --- | ---
[java][1] | Java JDK | 8
[maven][2] | Apache Maven | 3.3.x 
[oc][3] | OpenShift Client | v1.4.x
[git][4] | Git version management | 2.x 

[1]: http://www.oracle.com/technetwork/java/javase/downloads/
[2]: https://maven.apache.org/download.cgi?Preferred=ftp://mirror.reverse.net/pub/apache/
[3]: https://docs.openshift.com/enterprise/3.2/cli_reference/get_started_cli.html
[4]: https://git-scm.com/book/en/v2/Getting-Started-Installing-Git

In order to build and deploy this project on OpenShift, you need either:

* a local OpenShift instance such as Minishift,
* account on an OpenShift Online (OSO) instance, such as https://console.dev-preview-int.openshift.com/ instance.

# Deployment instructions

To build and deploy this quickstart you can:

1. run it locally (non in OpenShift),
2. deploy it to OpenShift using Apache Maven,
3. deploy it to OpenShift using a pipeline.
 
For the approach 2 and 3 you need to be logged in to your OpenShift instance.
 
**If you are using Minishift**

1. Login to your OpenShift instance using:

```bash
oc login https://192.168.64.12:8443 -u developer -p developer
```

2. Open your browser to https://192.168.64.12:8443/console/, and log in using _developer/developer_.

3. Check that you have a project. If `oc project` returns an error, create a project with:

```bash
oc new-project myproject
```

**If your are using OpenShift Online**
  
1. Go to [OpenShift Online](https://console.dev-preview-int.openshift.com/console/command-line) to get the token used 
by the `oc` client for authentication and project access.
2. On the oc client, execute the following command to replace $MYTOKEN with the one from the Web Console:
     
```bash
oc login https://api.dev-preview-int.openshift.com --token=$MYTOKEN
```

3. Check that you have a project. If `oc project` returns an error, create a project with:
   
```bash
oc new-project myproject
```

## Local run

To run this quickstart locally:

1. Execute the following Apache Maven command:

```bash
mvn clean package
```

The application is packaged as a _fat-jar_, _i.e._ a _jar_ containing all the required dependencies to run the 
application. So the resulting artifact is a standalone application.

2. Run the application using:
 
```bash
java -jar target/vertx-http-1.0.0-SNAPSHOT.jar
```
 
Alternatively, you can run it in _dev_ mode using `mvn compile vertx:run`.
 
3. Access the application using a browser: http://localhost:8080.
 
Alternatively, you can invoke the _greeting_ service directly using curl or httpie:
    
```bash
curl http://localhost:8080/api/greeting
curl http://localhost:8080/api/greeting?name=Bruno
http http://localhost:8080/api/greeting
http http://localhost:8080/api/greeting name==Charles
```

## Deploy the application to OpenShift using Maven

To deploy the application using Maven, launch:

```bash
mvn fabric8:deploy -Popenshift
```

This command builds and deploys the application to the OpenShift instance on which you are logged in.

Once deployed, you can access the application using the _application URL_. Retrieve it using:

```bash
$ oc get route vertx-http -o jsonpath={$.spec.host}
vertx-http-myproject.192.168.64.12.nip.io                                                                                                                              
```

Then, open your browser to the displayed url: http://vertx-http-myproject.192.168.64.12.nip.io.                                                                         

Alternatively, you can invoke the _greeting_ service directly using curl or httpie:
    
```bash
curl http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting
curl http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting?name=Bruno
http http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting
http http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting name==Charles
```

If you get a `503` response, it means that the application is not ready yet.

## Deploy the application to OpenShift using a pipeline

When deployed with a _pipeline_ the application is built from the sources (from a git repository) by a continuous 
integration server (Jenkins) running in OpenShift.

To trigger this built:

1. Apply the OpenShift template:

```bash
oc new-app -f src/openshift/openshift-pipeline-template.yml
```

2. Trigger the pipeline build:

```bash
oc start-build vertx-http
```

With the sequence of command, you have deployed a Jenkins instance in your OpenShift project, define the build 
pipeline of the application and trigger a first build of the application.

Once the build is complete, you can access the application using the _application URL_. Retrieve this url using:

```bash
oc get route vertx-http -o jsonpath={$.spec.host}
```

Then, open your browser to the displayed url. For instance, http://vertx-http-myproject.192.168.64.12.nip.io.           
                                                              
Alternatively, you can invoke the _greeting_ service directly using curl or httpie:
    
```bash
curl http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting
curl http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting?name=Bruno
http http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting
http http://vertx-http-myproject.192.168.64.12.nip.io/api/greeting name==Charles
```

If you get a `503` response, it means that the application is not ready yet.


# Running integration tests

The quickstart also contains a set of integration tests. You need to be connected to an OpenShift instance (Openshift 
Online or Minishift) to run them. You also need to select an existing project.

Then, run integration tests using:

```
mvn clean verify -Popenshift -Popenshift-it
```