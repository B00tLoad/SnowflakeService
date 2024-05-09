package space.b00tload.services.snowflake;

import space.b00tload.utils.configuration.ConfigValues;

public enum ConfigurationValues implements ConfigValues {
    MACHINE_ID_BITS("machinebits", "M", "MACHINE_ID_BITS", "bitcount.machine", "10"),
    SEQUENCE_BITS("sequencebits", "S", "SEQUENCE_BITS", "bitcount.sequence", "12"),
    EPOCH("epoch", "E", "EPOCH", "epoch", "1704067200000"),
    MACHINE_ID("machineid", "I", "MACHINE_ID", "machineid", "-1"),
    ORCHESTRATOR_IP("orchestrator", "O", "ORCHESTRATOR_IP", "orchestrator.ip", "localhost"),
    ;

    private final String cliFlag;
    private final String cliAlias;
    private final String envVariable;
    private final String tomlPath;
    private final String defaultValue;

    ConfigurationValues(String cliFlag, String cliAlias, String envVariable, String tomlPath, String defaultValue) {
        this.cliFlag = cliFlag;
        this.cliAlias = cliAlias;
        this.envVariable = envVariable;
        this.tomlPath = tomlPath;
        this.defaultValue = defaultValue;
    }

    /**
     * The command line flag for this config value.
     *
     * @return the command line flag
     * @example --discord-token
     */
    @Override
    public String getFlag() {
        return cliFlag;
    }

    /**
     * The command line alias for this config value.
     *
     * @return the command line alias
     * @example -d
     */
    @Override
    public String getFlagAlias() {
        return cliAlias;
    }

    /**
     * The environment variable name for this config value.
     *
     * @return the environment variable name
     * @example DISCORD_APP_TOKEN
     */
    @Override
    public String getEnvironmentVariable() {
        return envVariable;
    }

    /**
     * The path to the config value in a toml file.
     *
     * @return the toml path.
     * @example discord.token
     */
    @Override
    public String getTomlPath() {
        return tomlPath;
    }

    /**
     * The default value used if not set via other configuration means.
     *
     * @return the default value.
     */
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return "ConfigurationValues{" +
                "defaultValue='" + defaultValue + '\'' +
                ", tomlPath='" + tomlPath + '\'' +
                ", envVariable='" + envVariable + '\'' +
                ", cliAlias='" + cliAlias + '\'' +
                ", cliFlag='" + cliFlag + '\'' +
                '}';
    }
}
