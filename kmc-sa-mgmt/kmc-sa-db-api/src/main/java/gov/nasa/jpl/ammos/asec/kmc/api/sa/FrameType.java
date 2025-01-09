package gov.nasa.jpl.ammos.asec.kmc.api.sa;

/**
 * Frame Type enum
 */
public enum FrameType {
    /**
     * Telecommand
     */
    TC("SecAssn", SecAssn.class),
    /**
     * Telemetry
     */
    TM("SecAssnTm", SecAssnTm.class),
    /**
     * AOS (Advanced Orbiting Systems) Telemetry
     */
    AOS("SecAssnAos", SecAssnAos.class),
    /**
     * All
     */
    ALL("", null),
    UNKNOWN("Unknown", null);

    private String                    tableName;
    private Class<? extends ISecAssn> clazz;

    /**
     * Constructor
     *
     * @param tableName mysql table
     * @param clazz     implementing class
     */
    FrameType(final String tableName, Class<? extends ISecAssn> clazz) {
        this.tableName = tableName;
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return this.tableName;
    }

    /**
     * Get the implementing class
     *
     * @return implementing class
     */
    public Class<? extends ISecAssn> getClazz() {
        return clazz;
    }

    /**
     * Return frame type from string
     *
     * @param input
     * @return
     */
    public static FrameType fromString(String input) {
        return switch (input) {
            case "TC" -> TC;
            case "TM" -> TM;
            case "AOS" -> AOS;
            case "ALL" -> ALL;
            default -> UNKNOWN;
        };
    }
}
