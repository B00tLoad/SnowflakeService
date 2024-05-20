
# SnowflakeService

A tool/microservice to centrally generate snowflake IDs.

![GitHub License](https://img.shields.io/github/license/B00tLoad/SnowflakeService)
![GitHub Issues or Pull Requests](https://img.shields.io/github/issues/B00tLoad/SnowflakeService)
![GitHub repo size](https://img.shields.io/github/repo-size/B00tLoad/SnowflakeService)
![GitHub Release](https://img.shields.io/github/v/release/B00tLoad/SnowflakeService)
## Acknowledgements
This utility uses:
- The format and name of [Twitter's Snowflake IDs](https://blog.twitter.com/engineering/en_us/a/2010/announcing-snowflake)
- [Javalin](https://github.com/javalin/javalin)
- [QOS.CH's Logback](https://github.com/qos-ch/logback)
- [Google's gson](https://github.com/google/gson)
- [hyperxpro's Brotli4j](https://github.com/hyperxpro/Brotli4j)
- [B00tLoad_'s ConfigurationUtilities](https://github.com/B00tLoad/Configurationutilities)

## Installation

### Docker (Recommended)

```bash
  docker pull bootmediaalix/snowflakeservice
  docker run bootmediaalix/snowflakeservice -e %{set required .env, see below} -p 9567:9567 -v /data/b00tload-services/snowflake:%desired path on host%
```
### Containerless
A containerless installation is possible, although not supported. For development convenience the application base directory is located in `~/.b00tload-services/snowflake` instead of `/data/b00tload-services/snowflake`.
If you want to work containerless you are on your own.
## Environment Variables

To run this project, you may add the following environment variables to your .env file

`EPOCH` - the starting time of the snowflake (defaults to 01.01.2024 12:00 AM)

`MACHINE_ID_BITS` - the amount of bits used for the machine ID

`SEQUENCE_BITS` - the amount of bits used for the sequence counter

`MACHINE_ID` - the ID of the generator



## API Reference

#### Get an ID

```http
  GET /generate
```

Response example:
```json
{
    "id": "50990430426234880"
}
```

## Maintainer

- [@B00tLoad_](https://www.github.com/B00tLoad)


## License

[GNU GPL v3](https://github.com/B00tLoad/SnowflakeService/blob/master/LICENSE)


## Support

For support, [open a ticket](https://github.com/B00tLoad/SnowflakeService/issues) or email me at alix (at) ja-lol-ey (dot) de.

