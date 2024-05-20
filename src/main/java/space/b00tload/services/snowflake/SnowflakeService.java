package space.b00tload.services.snowflake;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter2;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.http.ContentType;
import io.javalin.http.HandlerType;
import io.javalin.router.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.b00tload.utils.configuration.Configuration;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * Main class orchestrating between generator, webserver and config
 *
 * @author Alix von Schirp
 * @version 1.0.0
 * @since 1.0.0
 */
public class SnowflakeService {

    /**
     * This software's version
     */
    private static String SOFTWARE_VERSION;
    /**
     * Base dir for config and tmp data
     */
    private static String APPLICATION_BASE;

    /**
     * Initializes tool, generator and webserver.
     * @param args cli args
     */
    public static void main(String[] args) {
        //Set up constants
        SOFTWARE_VERSION = Objects.requireNonNullElse(SnowflakeService.class.getPackage().getImplementationVersion(), "0.0.1-indev");
        APPLICATION_BASE = List.of(args).contains("--docker") ? Paths.get("data", "b00tload-tools", "snowflake").toString() : Paths.get(System.getProperty("user.home"), ".b00tload-tools", "snowflake").toString();

        //Set up logger
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        try{
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            loggerContext.reset();
            if(List.of(args).contains("--docker")) {
                configurator.doConfigure(Objects.requireNonNull(SnowflakeService.class.getResource("/config/logback/logback-docker.xml")));
            } else {
                configurator.doConfigure(Objects.requireNonNull(SnowflakeService.class.getResource("/config/logback/logback-bare.xml")));
            }
        } catch (JoranException ignored){}
        (new StatusPrinter2()).printInCaseOfErrorsOrWarnings(loggerContext);

        //Set up logger
        Logger LOGGER = LoggerFactory.getLogger(SnowflakeService.class);

        //Init config
        Configuration.init(args, SOFTWARE_VERSION, APPLICATION_BASE, ConfigurationValues.values());

        //Init webserver
        Javalin endpointServer = Javalin.create(config -> {
            config.http.brotliAndGzipCompression();
            config.http.prefer405over404 = true;
            config.requestLogger.http((ctx, executionTimeMs) -> {
                LoggerFactory.getLogger(config.getClass()).info("{} served in {}ms to {}(UA: \"{}\")", ctx.fullUrl(), executionTimeMs, ctx.req().getRemoteAddr(), ctx.userAgent());
            });
        });
        endpointServer.addEndpoint(new Endpoint(HandlerType.GET, "generate", ctx -> {
            JsonObject ret = new JsonObject();
            ret.addProperty("id", SnowflakeIDGenerator.getInstance().generateID());
            ctx.status(200).result(ret.toString()).contentType(ContentType.APPLICATION_JSON);
        }));
        endpointServer.start(9567);

    }
}
