# Api Plane
[![Hango gateway CI](https://github.com/hango-io/api-plane/actions/workflows/java_ci.yml/badge.svg)](https://github.com/hango-io/api-plane/actions/workflows/java_ci.yml)

## About
Generate Slime CRD and Istio CRD for gateway，include VirtualService, DestinationRule, EnvoyPlugin, etc...
VirtualService and DestinationRule are Istio CRD, which is used to route.
EnvoyPlugin and PluginManager are Slime CRD，which is used for plugin configuration.

## Project structure
```shell script
.
├── plugin
└── hango-api-plane-server
    ├── Dockerfile
    ├── src
    ├── setenv.sh
    └── pom.xml 
├── LICENSE
├── README.md
└── pom.xml

````

## Develop
Using maven
```shell script
$ git clone git@github.com:hango-io/api-plane.git
$ cd hango-api-plane-server
$ mvn clean package
```

## Contributing
If you wish to contribute to Hango API Gateway, please read the root projects' contributing files.

## License
[Apache-2.0](https://choosealicense.com/licenses/apache-2.0/)
