package gov.nasa.jpl.ammos.asec.kmc.api.sa;

public enum FrameType {
    TC("SecAssn", SecAssn.class), // telecommand
    TM("SecAssnTm", SecAssnTm.class), // telemetry
    AOS("SecAssnAos", SecAssnAos.class),
    UNKNOWN("Unknown", null); // advanced orbiting systems

    private String                    tableName;
    private Class<? extends ISecAssn> clazz;

    FrameType(final String tableName, Class<? extends ISecAssn> clazz) {
        this.tableName = tableName;
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return this.tableName;
    }

    public Class<? extends ISecAssn> getClazz() {
        return clazz;
    }

    public static FrameType fromString(String input) {
        return switch (input) {
            case "TC" -> TC;
            case "TM" -> TM;
            case "AOS" -> AOS;
            default -> UNKNOWN;
        };
    }
}
