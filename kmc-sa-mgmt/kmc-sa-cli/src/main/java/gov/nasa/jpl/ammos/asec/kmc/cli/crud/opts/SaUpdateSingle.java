package gov.nasa.jpl.ammos.asec.kmc.cli.crud.opts;

import picocli.CommandLine;

@CommandLine.Command
public class SaUpdateSingle {
    @CommandLine.Option(names = "--tfvn", required = false, description = "transfer frame version number")
    public Byte tfvn;

    @CommandLine.Option(names = "--scid", required = true, description = "spacecraft ID")
    public Short scid;

    @CommandLine.Option(names = "--vcid", required = false, description = "virtual channel ID")
    public Byte vcid;

    @CommandLine.Option(names = "--mapid", required = false, description = "multiplexer access ID")
    public Byte mapid;

    @CommandLine.Option(names = "--spi", required = true, description = "security parameter index")
    public Integer spi;
}
