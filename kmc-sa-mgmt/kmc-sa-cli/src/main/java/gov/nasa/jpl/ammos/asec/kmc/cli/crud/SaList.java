package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sadb.IKmcDao;
import gov.nasa.jpl.ammos.asec.kmc.cli.misc.Version;
import gov.nasa.jpl.ammos.asec.kmc.format.IOutput;
import gov.nasa.jpl.ammos.asec.kmc.format.SaCsvOutput;
import gov.nasa.jpl.ammos.asec.kmc.format.SaJsonOutput;
import gov.nasa.jpl.ammos.asec.kmc.format.SaMysqlOutput;
import picocli.CommandLine;

import java.util.List;
import java.util.stream.Collectors;

/**
 * List Security Assocations
 *
 */
@CommandLine.Command(name = "list", description = "List security associations", mixinStandardHelpOptions = true,
        versionProvider = Version.class)
public class SaList extends BaseCliApp {
    @CommandLine.Option(names = "--spi", description = "Filter by security parameter index")
    private Integer spi;
    @CommandLine.Option(names = "--scid", description = "Filter by spacecraft ID")
    private Short   scid;

    @CommandLine.ArgGroup(exclusive = true)
    private Output output;

    static class Output {
        @CommandLine.Option(names = {"-e", "--extended"}, description = "extended CSV format")
        private boolean extended;
        @CommandLine.Option(names = "--mysql", description = "MySQL-like key-value format")
        private boolean mysql;
        @CommandLine.Option(names = "--json", description = "JSON format")
        private boolean json;
    }

    @CommandLine.ArgGroup
    private ExclusiveActiveInactive filter;

    static class ExclusiveActiveInactive {
        @CommandLine.Option(names = "--active", description = "Only list active SAs")
        private boolean activeOnly;
        @CommandLine.Option(names = "--inactive", description = "Only list inactive SAs")
        private boolean inactiveOnly;
    }

    @Override
    public Integer call() throws Exception {
        try (IKmcDao dao = getDao()) {
            List<SecAssn> sas = null;
            if (filter != null) {
                if (filter.activeOnly) {
                    sas = dao.getSas().stream().filter(secAssn -> secAssn.getSaState() == 3).collect(Collectors.toList());
                } else if (filter.inactiveOnly) {
                    sas = dao.getSas().stream().filter(secAssn -> secAssn.getSaState() != 3).collect(Collectors.toList());
                }
            } else {
                sas = dao.getSas();
            }
            if (spi != null) {
                sas = sas.stream().filter(secAssn -> secAssn.getSpi().equals(spi)).collect(Collectors.toList());
            }
            if (scid != null) {
                sas = sas.stream().filter(secAssn -> secAssn.getScid().equals(scid)).collect(Collectors.toList());
            }

            IOutput out = null;
            if (output != null) {
                if (output.extended) {
                    out = new SaCsvOutput(true);
                } else if (output.json) {
                    out = new SaJsonOutput();
                } else if (output.mysql) {
                    out = new SaMysqlOutput();
                }
            } else {
                out = new SaCsvOutput(false);
            }

            out.print(spec.commandLine().getOut(), sas);
        }

        return 0;
    }

    public static void main(String... args) {
        int exitCode = new CommandLine(new SaList()).execute(args);
        System.exit(exitCode);
    }
}
