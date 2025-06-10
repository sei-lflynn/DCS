package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SpiScid;
import gov.nasa.jpl.ammos.asec.kmc.sadb.KmcDao;
import org.junit.Test;
import picocli.CommandLine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for starting SAs
 *
 */
public class SaStartTest extends BaseCommandLineTest {
    @Test
    public void testStart() throws KmcException {
        CommandLine cli  = getCmd(new SaStart(), true);
        int         exit = cli.execute();
        // no args
        assertNotEquals(0, exit);
        exit = cli.execute("--spi=2", "--scid=46");
        // already started
        assertNotEquals(0, exit);

        // start
        createExtraSas();
        exit = cli.execute("--spi", "8", "--scid", "46");
        assertEquals(0, exit);
        SecAssn sa1 = dao.getSa(new SpiScid(8, (short) 46));
        SecAssn sa2 = dao.getSa(new SpiScid(9, (short) 46));
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa1.getSaState());
        assertEquals(KmcDao.SA_UNKEYED, (short) sa2.getSaState());
    }

    @Test
    public void testStartAlreadyOperational() throws KmcException {
        // start when already active per GVCID
        createExtraSas();
        dao.startSa(new SpiScid(8, (short) 46), false);
        CommandLine cli  = getCmd(new SaStart(), true);
        int         exit = cli.execute("--scid", "46", "--spi", "9");
        assertNotEquals(0, exit);
    }

    @Test
    public void testStartForce() throws KmcException {
        createExtraSas();
        dao.startSa(new SpiScid(8, (short) 46), false);
        CommandLine cli  = getCmd(new SaStart(), true);
        int         exit = cli.execute("--scid", "46", "--spi", "9", "--force");
        assertEquals(0, exit);
    }

    @Test
    public void testStartMultiple() throws KmcException {
        createExtraSas();
        CommandLine cli  = getCmd(new SaStart(), true);
        int         exit = cli.execute("--scid", "46", "--spi", "8", "--spi", "10");
        assertEquals(0, exit);
        SecAssn sa1 = dao.getSa(new SpiScid(8, (short) 46));
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa1.getSaState());
        SecAssn sa2 = dao.getSa(new SpiScid(10, (short) 46));
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa2.getSaState());
    }

    @Test
    public void testStartMultipleFail() throws KmcException {
        createExtraSas();
        CommandLine cli  = getCmd(new SaStart(), true);
        int         exit = cli.execute("--scid", "46", "--spi", "8", "--spi", "9");
        assertNotEquals(0, exit);
        SecAssn sa1 = dao.getSa(new SpiScid(8, (short) 46));
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa1.getSaState());
        SecAssn sa2 = dao.getSa(new SpiScid(9, (short) 46));
        assertEquals(KmcDao.SA_UNKEYED, (short) sa2.getSaState());
    }

    private void createExtraSas() throws KmcException {
        dao.createSa(8, (byte) 0, (short) 46, (byte) 10, (byte) 0);
        dao.createSa(9, (byte) 0, (short) 46, (byte) 10, (byte) 0);
        dao.createSa(10, (byte) 0, (short) 46, (byte) 11, (byte) 0);
    }
}