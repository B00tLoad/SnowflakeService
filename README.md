
# SnowflakeService

A tool/microservice to centrally generate snowflake IDs. Can be run distributed.

![GitHub License](https://img.shields.io/github/license/B00tLoad/SnowflakeService)
![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/B00tLoad/SnowflakeService)
![GitHub repo size](https://img.shields.io/github/repo-size/B00tLoad/SnowflakeService)
![GitHub Release](https://img.shields.io/github/v/release/B00tLoad/SnowflakeService)
## Acknowledgements
This utility uses:
- The format and name of [Twitter's Snowflake IDs](https://blog.twitter.com/engineering/en_us/a/2010/announcing-snowflake)
- [Javalin](https://github.com/javalin/javalin)
- [QOS.CH's Logback](https://github.com/qos-ch/logback)
- [Square's OkHttp3](https://github.com/square/okhttp)
- [B00tLoad_'s ConfigurationUtilities](https://github.com/B00tLoad/Configurationutilities)

## Installation

### Docker (Recommended)

```bash
  docker pull bootmediaalix/snowflake-service
  docker run bootmediaalix/snowflake-service -e %{set required .env, see below} -p 95674:95674 -v /data/b00tload-services/snowflake:%desired path on host%
```
### Containerless
A containerless installation is possible, although not supported. For development convenience the application base directory is located in `~/.b00tload-services/snowflake` instead of `/data/b00tload-services/snowflake`.
If you want to work containerless you are on your own.
## Environment Variables

To run this project, you will need to add the following environment variables to your .env file

`EPOCH` - the starting time of the snowflake

`MACHINE_ID_BITS` - the amount of bits used for the machine ID

`SEQUENCE_BITS` - the amount of bits used for the sequence counter

`MACHINE_ID` - the ID of the generator

**_or_**

`ORCHESTRATOR_IP` - an IP where a snowflake orchestrator is available to fetch all above values

**_or_**

`EPOCH` - the starting time of the snowflake

`MACHINE_ID_BITS` - the amount of bits used for the machine ID

`SEQUENCE_BITS` - the amount of bits used for the sequence counter

with neither `MACHINE_ID` nor `ORCHESTRATOR_IP` set, the Service will start as an orchestrator itself with MACHINE_ID = 0.



## API Reference

#### Get an ID

```http
  GET /flake
```

Response example:
```json
{
    "snowflake": ""
}
```

#### Register worker (internal)

```http request
POST /orchestra/register
```

| Header | Description                                                                       |
|:-------|:----------------------------------------------------------------------------------|
| `name` | **Required**. A name to recognize the worker by (e.g. hostname+pid+random number) |

Response example:
Http Status: 200
```json
{
  "machineBits": "",
  "workerid": "",
  "sequenceBits": "",
  "epoch": ""
}
```
Response on error:
If the version of the client and the orchestrator do not match the orchestrator will respond with a HTTP code 426. The required version will be 

| Value name               | Value description         |
|:-------------------------|:--------------------------|
| Http Status              | 426 - Upgrade required    |
| `Upgrade` Header content | required software version |

| Value name               | Value description         |
|:-------------------------|:--------------------------|
| Http Status              | 409 - Conflict            |
| `Upgrade` Header content | required software version |
#### Worker heartbeat (internal)
```http request
GET /orchestra/heartbeat
```
| Parameter   | Description                               |
|:------------|:------------------------------------------|
| `name`      | the ID used in registration               |
| `workerID`  | the workerID assigned by the orchestrator |

HTTP status code responses:

| Status | Description                 |
|:-------|:----------------------------|
| `200`  | Ok                          |
| `409`  | Conflict, please reregister |

## Maintainer

- [@B00tLoad_](https://www.github.com/B00tLoad)


## License

[GNU GPL v3](https://github.com/B00tLoad/SnowflakeService/blob/master/LICENSE)


## Support

For support, open a ticket or email me at alix (at) ja-lol-ey (dot) de.

