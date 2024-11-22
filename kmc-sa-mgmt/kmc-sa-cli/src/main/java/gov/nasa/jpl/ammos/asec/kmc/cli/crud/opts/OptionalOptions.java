package gov.nasa.jpl.ammos.asec.kmc.cli.crud.opts;

import picocli.CommandLine;

@CommandLine.Command
public class OptionalOptions {
    @CommandLine.Option(names = "--ekid", required = false, description = "encryption key ID")
    public String ekid;

    @CommandLine.Option(names = "--ecs", required = false, description = "encryption cypher suite (hex string)")
    public String ecs;

    @CommandLine.Option(names = "--akid", required = false, description = "authentication key ID")
    public String akid;

    @CommandLine.Option(names = "--acs", required = false, description = "authentication cypher suite")
    public String acs;

    @CommandLine.Option(names = "--iv", required = false, description = "initialization vector (hex string)")
    public String iv;

    @CommandLine.Option(names = "--ivlen", required = false, description = "initialization vector length, in bytes",
            defaultValue = "12")
    public Short ivLen;

    @CommandLine.Option(names = "--arsn", required = false, description =
            "anti-replay sequence number starting " + "value (hex string)")
    public String arsn;

    @CommandLine.Option(names = "--arsnlen", required = false, description = "anti-replay sequence number length "
            + "(bytes)")
    public Short arsnlen;

    @CommandLine.Option(names = "--arsnw", required = false, description = "anti-replay sequence number window")
    public Short arsnw;

    @CommandLine.Option(names = "--abm", required = false, description = "authentication bitmask (hex string)")
    public String abm;

    @CommandLine.Option(names = "--abmlen", required = false, description = "authentication bitmask length (bytes)")
    public Integer abmLen;

    @CommandLine.Option(names = "--shivflen", required = false, description =
            "security header initialization " + "vector field length (bytes)")
    public Short shivfLen;

    @CommandLine.Option(names = "--shsnflen", required = false, description =
            "security header sequence number " + "field length (bytes)")
    public Short shsnfLen;

    @CommandLine.Option(names = "--shplflen", required = false, description =
            "security header pad length field " + "length (bytes)")
    public Short shplfLen;

    @CommandLine.Option(names = "--stmacflen", required = false, description = "security trailer MAC field " +
            "length" + " (bytes)")
    public Short stmacfLen;

    @CommandLine.Option(names = "--st", required = false, description = "service type. 0 = PLAINTEXT, 1 = " +
            "ENCRYPTION, 2 = AUTHENTICATION, 3 = AUTHENTICATED_ENCRYPTION")
    public String st;
}
