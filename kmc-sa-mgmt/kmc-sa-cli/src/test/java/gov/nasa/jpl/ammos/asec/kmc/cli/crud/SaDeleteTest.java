package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssn;
import gov.nasa.jpl.ammos.asec.kmc.sadb.BaseH2Test;
import org.junit.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SaDeleteTest extends BaseH2Test {

    @Test
    public void testDelete() throws KmcException {
        CommandLine   cli = getCmd(true, null, null);
        List<SecAssn> sas = dao.getSas();
        assertEquals(5, sas.size());
        cli.execute("--spi=1", "--scid=46", "-y");
        sas = dao.getSas();
        assertEquals(4, sas.size());
        for (SecAssn sa : sas) {
            assertNotEquals(1, (int) sa.getId().getSpi());
        }
    }

    @Test
    public void testDeleteConfirm() throws KmcException {
        InputStream old = System.in;
        InputStream is  = new ByteArrayInputStream("y".getBytes(StandardCharsets.UTF_8));
        System.setIn(is);
        CommandLine   cli = getCmd(true, null, null);
        List<SecAssn> sas = dao.getSas();
        assertEquals(5, sas.size());
        cli.execute("--spi=1", "--scid=46");
        sas = dao.getSas();
        assertEquals(4, sas.size());
        for (SecAssn sa : sas) {
            assertNotEquals(1, (int) sa.getId().getSpi());
        }
        System.setIn(old);
    }

    @Test
    public void testDeleteRefuse() throws KmcException {
        InputStream old = System.in;
        InputStream is  = new ByteArrayInputStream("n".getBytes(StandardCharsets.UTF_8));
        System.setIn(is);
        CommandLine   cli = getCmd(true, null, null);
        List<SecAssn> sas = dao.getSas();
        assertEquals(5, sas.size());
        cli.execute("--spi=1", "--scid=46");
        sas = dao.getSas();
        assertEquals(5, sas.size());
        System.setIn(old);
    }

    @Test
    public void testDeleteMulti() throws KmcException {
        CommandLine   cli = getCmd(true, null, null);
        List<SecAssn> sas = dao.getSas();
        assertEquals(5, sas.size());
        cli.execute("--spi=1", "--spi=2", "--scid=46", "-y");
        sas = dao.getSas();
        assertEquals(3, sas.size());
        for (SecAssn sa : sas) {
            assertNotEquals(1, (int) sa.getId().getSpi());
            assertNotEquals(2, (int) sa.getId().getSpi());
        }
    }

    @Test
    public void testDeleteDne() {
        CommandLine cli  = getCmd(true, null, null);
        int         exit = cli.execute("--spi=6", "--scid=55");
        assertNotEquals(0, exit);
    }

    @Test
    public void testDeleteFail() throws KmcException {
        List<SecAssn> sas = dao.getSas();
        assertEquals(5, sas.size());

        CommandLine cli  = getCmd(true, null, null);
        int         exit = cli.execute("--spi=1");
        assertNotEquals(0, exit);

        exit = cli.execute("--scid=46");
        assertNotEquals(0, exit);

        sas = dao.getSas();
        assertEquals(5, sas.size());
    }

    private CommandLine getCmd(boolean overrideOutput, PrintWriter out, PrintWriter err) {
        SaDelete    create = new SaDelete();
        CommandLine cmd    = new CommandLine(create);
        if (overrideOutput) {
            if (out == null) {
                out = new PrintWriter(new StringWriter());
            }
            cmd.setOut(out);
            if (err == null) {
                err = new PrintWriter(new StringWriter());
            }
            cmd.setErr(err);
        }
        return cmd;
    }
}