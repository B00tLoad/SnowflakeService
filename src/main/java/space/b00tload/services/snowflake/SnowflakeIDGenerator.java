package space.b00tload.services.snowflake;

import org.slf4j.LoggerFactory;

public class SnowflakeIDGenerator {

    // Constants
    private static final long EPOCH = 1704067200000L; // January 1, 2024, 00:00:00 UTC
    private static final long MACHINE_ID_BITS = 10L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_MACHINE_ID = (1L << MACHINE_ID_BITS) - 1;
    private static final long MAX_SEQUENCE = (1L << SEQUENCE_BITS) - 1;

    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;

    // State
    private final long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    // Constructor
    public SnowflakeIDGenerator(long machineId) {
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }
        this.machineId = machineId;
    }

    // Method to generate a new ID
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

        return ((currentTimestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (machineId << MACHINE_ID_SHIFT)
                | sequence;
    }

    // Get the current timestamp in milliseconds
    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    // Wait for the next timestamp
    private long waitForNextTimestamp(long currentTimestamp) {
        long nextTimestamp = currentTimeMillis();
        while (nextTimestamp <= currentTimestamp) {
            nextTimestamp = currentTimeMillis();
        }
        return nextTimestamp;
    }
}


