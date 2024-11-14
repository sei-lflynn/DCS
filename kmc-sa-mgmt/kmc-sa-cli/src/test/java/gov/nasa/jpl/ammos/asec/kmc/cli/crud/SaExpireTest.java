package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ISecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SpiScid;
import org.junit.Before;
import org.junit.Test;
import picocli.CommandLine;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SaExpireTest extends BaseCommandLineTest {

    private CommandLine cli;

    @Before
    public void setupTest() {
        cli = getCmd(new SaExpire(), true);
    }

    @Test
    public void testExpireNoConfirm() throws KmcException {
        int exit = cli.execute("--spi=1", "--scid=46", "-y");
        assertEquals(0, exit);
        ISecAssn sa = dao.getSa(new SpiScid(1, (short) 46));
        assertEquals(1, (short) sa.getSaState());
    }

    @Test
    public void testExpireConfirm() throws KmcException {
        InputStream old = System.in;
        InputStream in  = new ByteArrayInputStream("y".getBytes(StandardCharsets.UTF_8));
        System.setIn(in);
        int exit = cli.execute("--spi=1", "--scid=46");
        assertEquals(0, exit);
        ISecAssn sa = dao.getSa(new SpiScid(1, (short) 46));
        assertEquals(1, (short) sa.getSaState());
        System.setIn(old);
    }

    @Test
    public void testExpireRefuse() throws KmcException {
        InputStream old = System.in;
        InputStream in  = new ByteArrayInputStream("n".getBytes(StandardCharsets.UTF_8));
        System.setIn(in);
        int exit = cli.execute("--spi=1", "--scid=46");
        assertEquals(0, exit);
        ISecAssn sa = dao.getSa(new SpiScid(1, (short) 46));
        assertEquals(3, (short) sa.getSaState());
        System.setIn(old);
    }

    @Test
    public void testExpireFail() {
        int exit = cli.execute("--spi=8");
        assertNotEquals(0, exit);
        exit = cli.execute("--scid=46");
        assertNotEquals(0, exit);
        exit = cli.execute("--spi=8", "--scid=46");
        assertNotEquals(0, exit);
    }
}