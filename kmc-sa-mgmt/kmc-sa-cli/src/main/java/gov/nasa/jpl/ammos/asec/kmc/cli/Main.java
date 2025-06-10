package gov.nasa.jpl.ammos.asec.kmc.cli;

import gov.nasa.jpl.ammos.asec.kmc.cli.crud.*;
import gov.nasa.jpl.ammos.asec.kmc.cli.misc.Version;
import picocli.CommandLine;

/**
 * Main CLI launcher class
 *
 */
@CommandLine.Command(name = "kmc-sa-mgmt", subcommands = {SaList.class,
        SaCreate.class,
        SaUpdate.class,
        SaDelete.class,
        SaKey.class,
        SaStart.class,
        SaStop.class,
        SaExpire.class}, description = "KMC Security Association Management CLI", mixinStandardHelpOptions = true,
        versionProvider = Version.class)
public class Main {
    public static void main(String... args) {
        int exit =
                new CommandLine(new Main()).setExecutionExceptionHandler(new PrintExceptionMessageHandler()).execute(args);
        System.exit(exit);
    }
}

/**
 * Exception handler
 *
 */
class PrintExceptionMessageHandler implements CommandLine.IExecutionExceptionHandler {
    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) throws Exception {
        commandLine.getErr().println(commandLine.getColorScheme().errorText(ex.getMessage()));
        throw ex;
        /*return commandLine.getExitCodeExceptionMapper() != null ?
                commandLine.getExitCodeExceptionMapper().getExitCode(ex) :
                commandLine.getCommandSpec().exitCodeOnExecutionException();
                */
    }
}
