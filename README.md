# comparisons

## Configuration

Required environment variables:

* `AZURE_STORAGE_CONN_STRING`: A connection string for an Azure Storage Account

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
mvn spring-boot:run
```