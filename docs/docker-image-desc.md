# Short Description

The PEP provides service for segmenting CCD documents according to a patient's granular consent. 

# Full Description

# Supported Source Code Tags and Current `Dockerfile` Link

[`2.1.0 (latest)`](https://github.com/bhits-dev/pep/releases/tag/2.1.0), [`2.0.0`](https://github.com/bhits-dev/pep/releases/tag/2.0.0)

[`Current Dockerfile`](../pep/src/main/docker/Dockerfile)

For more information about this image, the source code, and its history, please see the [GitHub repository](https://github.com/bhits-dev/pep).

# What is PEP?

The Policy Enforcement Point (PEP) Service is one of the core components of the Consent2Share(C2S) application. 
The PEP delegates the access decision to the Context Handler API, and it utilizes the Document Segmentation Service 
([DSS](https://github.com/bhits-dev/dss-api)) for segmenting CCD documents according to a patient's granular consent. 
PEP gives the same response for both *"No applicable consents"* and *"No documents found"* cases to avoid exposing the existence of a patient's consent.

For more information and related downloads for Consent2Share, please visit [Consent2Share](https://bhits-dev.github.io/consent2share/).
# How to use this image

## Start a PEP instance

Be sure to familiarize yourself with the repository's [README.md](https://github.com/bhits-dev/pep) file before starting the instance.

`docker run  --name pep -d bhitsdev/pep:latest <additional program arguments>`

*NOTE: In order for this API to fully function as a microservice in the Consent2Share application, it is required to setup the dependency microservices and the support level infrastructure. Please refer to the Consent2Share Deployment Guide in the corresponding Consent2Share release (see [Consent2Share Releases Page](https://github.com/bhits-dev/consent2share/releases)) for instructions to setup the Consent2Share infrastructure.*
## Configure

The Spring profiles `application-default` and `docker` are activated by default when building images.

This API can run with the default configuration which is from three places: `bootstrap.yml`, `application.yml`, and the data which the [`Configuration Server`](https://github.com/bhits-dev/config-server) reads from the `Configuration Data Git Repository`. Both `bootstrap.yml` and `application.yml` files are located in the class path of the running application.

We **recommend** overriding the configuration as needed in the `Configuration Data Git Repository`, which is used by the `Configuration Server`.

Also, [Spring Boot](https://projects.spring.io/spring-boot/) supports other ways to override the default configuration to configure the API for a certain deployment environment. 

The following is an example to override the default database password:

`docker run -d bhitsdev/pep:latest --spring.datasource.password=strongpassword`

## Environment Variables

When you start the PEP image, you can edit the configuration of the PEP instance by passing one or more environment variables on the command line. 

### JAR_FILE

This environment variable is used to setup which jar file will run. you need mount the jar file to the root of container.

`docker run --name pep -e JAR_FILE="pep-latest.jar" -v "/path/on/dockerhost/pep-latest.jar:/pep-latest.jar" -d bhitsdev/pep:latest`

### JAVA_OPTS 

This environment variable is used to setup JVM argument, such as memory configuration.

`docker run --name pep -e "JAVA_OPTS=-Xms512m -Xmx700m -Xss1m" -d bhitsdev/pep:latest`

### DEFAULT_PROGRAM_ARGS 

This environment variable is used to setup an application argument. The default value of is "--spring.profiles.active=application-default, docker".

`docker run --name pep -e DEFAULT_PROGRAM_ARGS="--spring.profiles.active=application-default,ssl,docker" -d bhitsdev/pep:latest`

# Supported Docker versions

This image is officially supported on Docker version 1.12.1.

Support for older versions (down to 1.6) is provided on a best-effort basis.

Please see the [Docker installation documentation](https://docs.docker.com/engine/installation/) for details on how to upgrade your Docker daemon.

# License

View [license](https://github.com/bhits-dev/pep/blob/master/LICENSE) information for the software contained in this image.

# User Feedback

## Documentation 

Documentation for this image is stored in the [bhitsdev/pep](https://github.com/bhits-dev/pep) GitHub repository. Be sure to familiarize yourself with the repository's README.md file before attempting a pull request.

## Issues

If you have any problems with or questions about this image, please contact us through a [GitHub issue](https://github.com/bhits-dev/pep/issues).