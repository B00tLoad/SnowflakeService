package space.b00tload.services.snowflake;

import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import space.b00tload.utils.configuration.Configuration;

import java.util.Objects;

import static java.lang.System.currentTimeMillis;

/**
 * Generates the SnowflakeIDs. Must be initialized with a machine ID between 0 and 1023.<br>
 * Can generate up to 4096 IDs per millisecond. If this limit is hit, generation will be halted until the next millisecond.<br>
 * Snowflakes always have a max size of 63 bits and can therefore be saved as a {@code long} (64-bit integer).
 *
 * @author Alix von Schirp
 * @since 1.0.0
 * @version 1.0.0
 */
public class SnowflakeIDGenerator {

    //Singleton instance
    private static SnowflakeIDGenerator INSTANCE;

    // Constants
    /**
     * Epoch for this generator.
     * Default is {@code January 1, 2024, 00:00:00 UTC}.
     */
    private final long EPOCH;
    /**
     * Amount of bits reserved for machine ID.
     * Default is 10.
     */
    private final long MACHINE_ID_BITS;
    /**
     * Amount of bits reserved for sequence counter.
     * Default is 12.
     */
    private final long SEQUENCE_BITS;
    /**
     * Amount of bits reserved for timestamp.
     * Default is 41.
     */
    private final long TIMESTAMP_BITS;
    /**
     * The calculated maximum machine ID.
     * Defaults to 1023.
     */
    private final long MAX_MACHINE_ID;
    /**
     * The calculated maximum sequence counter value.
     * Defaults to 4095.
     */
    private final long MAX_SEQUENCE;
    /**
     * The calculated maximum timestamp.
     * Defaults to 2199023255551, which gives roughly 69 years.
     */
    private final long MAX_TIMESTAMP;
    /**
     * How far to shift the machine ID (timestamp + machine + <b><i>sequence</i></b>)
     */
    private final long MACHINE_ID_SHIFT;
    /**
     * How far to shift the timestamp (timestamp + <b><i>machine + sequence</i></b>
     */
    private final long TIMESTAMP_SHIFT;

    // State
    /**
     * The ID used by the machine.
     */
    private final long machineId;
    /**
     * The sequence counter.
     */
    private long sequence = 0L;
    /**
     * The unix timestamp at which the last ID was generated.
     */
    private long lastTimestamp = -1L;

    /**
     * Used for initiating constants and state and performing bound checks on {@code EPOCH}, {@code MAX_TIMESTAMP} and {@code machineId}.
     *
     * @param machineId The machineID used for generation // expected to be null if not initialized by orchestrator, loaded from config
     * @param epoch The snowflake epoch used for generation // expected to be null if not initialized by orchestrator, loaded from config
     * @param machineIdBits The amount of bits used for the machineID // expected to be null if not initialized by orchestrator, loaded from config
     * @param sequenceBits The amount of bits used for sequence counter // expected to be null if not initialized by orchestrator, loaded from config
     * @throws IllegalArgumentException if any bound checks fail.
     */
    private SnowflakeIDGenerator(@Nullable Long machineId, @Nullable Long epoch, @Nullable Long machineIdBits, @Nullable Long sequenceBits) throws IllegalArgumentException{
        //init constants
        MACHINE_ID_BITS = Objects.requireNonNullElse(machineIdBits,
                Long.parseLong(Configuration.getInstance().get(ConfigurationValues.MACHINE_ID_BITS)));
        MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1;

        SEQUENCE_BITS = Objects.requireNonNullElse(sequenceBits,
                Long.parseLong(Configuration.getInstance().get(ConfigurationValues.SEQUENCE_BITS)));
        MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

        TIMESTAMP_BITS = 63L - MACHINE_ID_BITS - SEQUENCE_BITS;
        MAX_TIMESTAMP = (1L << TIMESTAMP_BITS) -1;

        EPOCH = Objects.requireNonNullElse(epoch,
                Long.parseLong(Configuration.getInstance().get(ConfigurationValues.EPOCH)));

        MACHINE_ID_SHIFT = SEQUENCE_BITS;
        TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

        //init state
        this.machineId = Objects.requireNonNullElse(machineId,
                Long.parseLong(Configuration.getInstance().get(ConfigurationValues.MACHINE_ID)));

        //Bound checks
        if(EPOCH > currentTimeMillis()){
            throw new IllegalArgumentException("Epoch may not be in the future.");
        }
        if(EPOCH+MAX_TIMESTAMP <= currentTimeMillis()){
            throw new IllegalArgumentException("Latest possible timestamp may not be in the past.");
        }
        if (this.machineId < 0 || this.machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }
    }

    /**
     * Generates a new snowflake ID.<br>
     * It uses a long (64-bit signed int) made up of (in default configuration):
     * <table>
     *     <tr>
     *         <th>bit(s)</th>
     *         <th>content</th>
     *     </tr>
     *     <tr>
     *         <td>0</td>
     *         <td>0 (signed positive)</td>
     *     </tr>
     *     <tr>
     *         <td>1-41</td>
     *         <td>timestamp (<code>current unix time</code> - <code>epoch</code>)</td>
     *     </tr>
     *     <tr>
     *         <td>42-51</td>
     *         <td>machineID</td>
     *     </tr>
     *     <tr>
     *         <td>52-63</td>
     *         <td>sequence</td>
     *     </tr>
     * </table>
     * Generation will be halted
     * <ul>
     *     <li>if the clock moves backwards (e.g. fast clock after ntp synchronization) until {@code last generated milli}<{@code current milli}</li>
     *     <li>if {@code count(generated IDs this millisecond)} is greater than {@code MAX_SEQUENCE} until next millisecond</li>
     * </ul>
     * @return the generated ID
     */
    public synchronized long generateID() {
        long currentTimestamp = currentTimeMillis();

        if (currentTimestamp < lastTimestamp) {
            LoggerFactory.getLogger(SnowflakeIDGenerator.class).warn("Clock is moving backwards. Waiting to generate ID.");
            while (currentTimestamp < lastTimestamp) {
                currentTimestamp = currentTimeMillis();
            }
        }

        if (currentTimestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitForNextTimestamp(currentTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        return  ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }



    // Wait for the next timestamp
    private long waitForNextTimestamp(long currentTimestamp) {
        long nextTimestamp = currentTimeMillis();
        while (nextTimestamp <= currentTimestamp) {
            nextTimestamp = currentTimeMillis();
        }
        return nextTimestamp;
    }

    public static void init(long machineId, long epoch, long machineIdBits, long sequenceBits){
        INSTANCE = new SnowflakeIDGenerator(machineId, epoch, machineIdBits, sequenceBits);
    }

    public static void init(){
        INSTANCE = new SnowflakeIDGenerator(null, null, null, null);
    }

    public static SnowflakeIDGenerator getInstance() {
        if(Objects.isNull(INSTANCE)) throw new UnsupportedOperationException("SnowflakeIDGenerator is not initialized.");
        return INSTANCE;
    }

    public long getEPOCH() {
        return EPOCH;
    }

    public long getMACHINE_ID_BITS() {
        return MACHINE_ID_BITS;
    }

    public long getSEQUENCE_BITS() {
        return SEQUENCE_BITS;
    }

    public long getMAX_MACHINE_ID() {
        return MAX_MACHINE_ID;
    }

    public long getMAX_SEQUENCE() {
        return MAX_SEQUENCE;
    }

    public long getMACHINE_ID_SHIFT() {
        return MACHINE_ID_SHIFT;
    }

    public long getTIMESTAMP_SHIFT() {
        return TIMESTAMP_SHIFT;
    }

    public long getMachineId() {
        return machineId;
    }

    public long getTIMESTAMP_BITS() {
        return TIMESTAMP_BITS;
    }

    public long getMAX_TIMESTAMP() {
        return MAX_TIMESTAMP;
    }
}


