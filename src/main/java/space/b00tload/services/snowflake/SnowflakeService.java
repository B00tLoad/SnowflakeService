package space.b00tload.services.snowflake;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter2;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.b00tload.utils.configuration.Configuration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class SnowflakeService {

    private static String SOFTWARE_VERSION;
    private static String APPLICATION_BASE;
    private static String USER_AGENT;
    private static Logger LOGGER;
    private static String INSTANCE_NAME;

    public static void main(String[] args) {
        //Set up constants
        SOFTWARE_VERSION = Objects.requireNonNullElse(SnowflakeService.class.getPackage().getImplementationVersion(), "0.0.1-indev");
        USER_AGENT = "SnowflakeService " + SOFTWARE_VERSION + "(" + System.getProperty("os.name") + "; " + System.getProperty("os.arch") + ") Java/" + System.getProperty("java.version");
        APPLICATION_BASE = List.of(args).contains("--docker") ? Paths.get("data", "b00tload-tools", "snowflake").toString() : Paths.get(System.getProperty("user.home"), ".b00tload-tools", "snowflake").toString();
        INSTANCE_NAME = getHostname() + "_" + getPid() + "_" + new Random().nextInt(9999);

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
        LOGGER = LoggerFactory.getLogger(SnowflakeService.class);

        //Init config
        Configuration.init(args, SOFTWARE_VERSION, APPLICATION_BASE, ConfigurationValues.values());

        long machineID;
        long sequenceBits;
        long machineBits;
        long epoch;

        if(!Configuration.getInstance().get(ConfigurationValues.ORCHESTRATOR_IP).equals(ConfigurationValues.ORCHESTRATOR_IP.getDefaultValue())){
            OkHttpClient httpClient = new OkHttpClient();
            try {
                try (Response r = httpClient.newCall(
                        new Request.Builder()
                                .url(Configuration.getInstance().get(ConfigurationValues.ORCHESTRATOR_IP))
                                .post(
                                    new FormBody.Builder().addEncoded("name", INSTANCE_NAME).build()
                                )
                                .build()
                ).execute()){
                    if(r.code() == 200){

                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if((Long.parseLong(Configuration.getInstance().get(ConfigurationValues.MACHINE_ID))) != -1L) {

        } else {

        }

    }

    private static String getHostname(){
        String hostname = System.getenv("COMPUTERNAME"); // On Windows
        if (hostname == null || hostname.isEmpty()) {
            hostname = System.getenv("HOSTNAME"); // On Unix/Linux
        }
        return hostname;
    }

    private static long getPid(){
        return ProcessHandle.current().pid();
    }

}
