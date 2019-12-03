# comparisons

## Configuration

Required environment variables:

* `AZURE_STORAGE_CONN_STRING`: A connection string for an Azure Storage Account
* `AZURE_SEARCH_ENDPOINT`: An Azure Cognitive Search endpoint
* `AZURE_SEARCH_KEY`: An Azure Cognitive Search admin key
* `ELASTICSEARCH_HOST`: An Elasticsearch host

## Development

### Docker Compose

1. Copy `.env.template` to `.env` and replace with your own values
2. `docker-compose up`

### Local environment

Tested with:
* Java 11
* Maven 3.6.2

```shell
export AZURE_STORAGE_CONN_STRING="<your_connection_string>"
# ... set other required env vars here

mvn spring-boot:run
```

### Tip: Consuming unpublished libraries

Below are some useful commands when working with libraries that aren't published yet

#### Get the short hash of a Git commit

```shell
git rev-parse --short HEAD
```

#### Install a JAR into the local Maven repository

```shell
mvn install:install-file -DgroupId=com.example -DartifactId=my-library -Dversion=1.0.0 -Dpackaging=jar -Dfile=/lib/my-library.jar
```