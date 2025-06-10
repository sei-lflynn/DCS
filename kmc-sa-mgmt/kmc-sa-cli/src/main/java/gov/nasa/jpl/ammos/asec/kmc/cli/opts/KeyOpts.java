package gov.nasa.jpl.ammos.asec.kmc.cli.opts;

import picocli.CommandLine;

public class KeyOpts {
    @CommandLine.Option(names = "--ekid", required = false, description = "encryption key ID")
    public String ekid;

    @CommandLine.Option(names = "--ecs", required = false, description = "encryption cypher suite (hex string)")
    public String ecs;

    @CommandLine.Option(names = "--ecslen", required = false, description = "ecs byte length")
    public Short ecsLen;

    @CommandLine.Option(names = "--akid", required = false, description = "authentication key ID")
    public String akid;

    @CommandLine.Option(names = "--acs", required = false, description = "authentication cypher suite")
    public String acs;

    @CommandLine.Option(names = "--acslen", required = false, description = "acs length (bytes)")
    public Short acsLen;
}
