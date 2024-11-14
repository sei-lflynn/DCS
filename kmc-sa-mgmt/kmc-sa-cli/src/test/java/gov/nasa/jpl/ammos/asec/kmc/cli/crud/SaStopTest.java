package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ISecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SpiScid;
import gov.nasa.jpl.ammos.asec.kmc.sadb.KmcDao;
import org.junit.Test;
import picocli.CommandLine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for stopping SAs
 */
public class SaStopTest extends BaseCommandLineTest {

    @Test
    public void testStop() throws KmcException {
        SpiScid     id   = new SpiScid(1, (short) 46);
        CommandLine cli  = getCmd(new SaStop(), true);
        int         exit = cli.execute();
        // no args
        assertNotEquals(0, exit);
        ISecAssn sa = dao.getSa(id);
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa.getSaState());
        exit = cli.execute("--scid", "46", "--spi", "1");
        assertEquals(0, exit);
        sa = dao.getSa(id);
        assertEquals(KmcDao.SA_KEYED, (short) sa.getSaState());
    }

    @Test
    public void testStopMultiple() throws KmcException {
        SpiScid  id1 = new SpiScid(1, (short) 46);
        ISecAssn sa1 = dao.getSa(id1);
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa1.getSaState());
        SpiScid  id2 = new SpiScid(2, (short) 46);
        ISecAssn sa2 = dao.getSa(id2);
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa2.getSaState());

        CommandLine cli  = getCmd(new SaStop(), true);
        int         exit = cli.execute("--scid", "46", "--spi", "1", "--spi", "2");
        assertEquals(0, exit);
        sa1 = dao.getSa(id1);
        assertEquals(KmcDao.SA_KEYED, (short) sa1.getSaState());
        sa2 = dao.getSa(id2);
        assertEquals(KmcDao.SA_KEYED, (short) sa2.getSaState());
    }

    @Test
    public void testStopDne() {
        CommandLine cli  = getCmd(new SaStop(), true);
        int         exit = cli.execute("--scid=40", "--spi=1");
        assertNotEquals(0, exit);
    }

    @Test
    public void testAlreadyStopped() throws KmcException {
        SpiScid  id1 = new SpiScid(1, (short) 46);
        ISecAssn sa1 = dao.getSa(id1);
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa1.getSaState());
        dao.stopSa(id1);
        sa1 = dao.getSa(id1);
        assertEquals(KmcDao.SA_KEYED, (short) sa1.getSaState());
        CommandLine cli  = getCmd(new SaStop(), true);
        int         exit = cli.execute("--scid=40", "--spi=1");
        assertNotEquals(0, exit);
    }

}