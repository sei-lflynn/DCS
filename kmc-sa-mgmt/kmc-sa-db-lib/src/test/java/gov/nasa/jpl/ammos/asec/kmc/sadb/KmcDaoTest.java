package gov.nasa.jpl.ammos.asec.kmc.sadb;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcStartException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SpiScid;
import gov.nasa.jpl.ammos.asec.kmc.api.sadb.IDbSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for KMC DAO
 *
 */
public class KmcDaoTest extends BaseH2Test {

    @Test(expected = KmcException.class)
    public void testConnectionFail() throws KmcException {
        KmcDao fail = new KmcDao();
        fail.init();
    }

    @Test
    public void testCreateDeleteSa() throws KmcException {
        dao.createSa(6, (byte) 0, (short) 255, (byte) 1, (byte) 0);
        List<SecAssn> sas = dao.getSas();
        assertNotNull(sas);
        assertEquals(6, sas.size());
        SecAssn sa = sas.get(5);
        assertEquals(6, (int) sa.getSpi());
        assertEquals(1, (short) sa.getSaState());
        dao.deleteSa(sas.get(5).getId());
        sas = dao.getSas();
        assertNotNull(sas);
        assertEquals(5, sas.size());
    }

    @Test
    public void testCreateDeleteSa2() throws KmcException {
        SecAssn sa = new SecAssn(new SpiScid(6, (short) 255));
        sa.setTfvn((byte) 0);
        sa.setVcid((byte) 1);
        sa.setMapid((byte) 0);
        dao.createSa(sa);
        List<SecAssn> sas = dao.getSas();
        assertNotNull(sas);
        assertEquals(6, sas.size());
        sa = sas.get(5);
        assertEquals(6, (int) sa.getSpi());
        assertEquals(1, (short) sa.getSaState());
        assertEquals(255, (short) sa.getScid());
        assertEquals(1, (short) sa.getVcid());
        dao.deleteSa(sa.getId());
        sas = dao.getSas();
        assertEquals(5, sas.size());
    }

    @Test
    public void testCreateDeleteSaNonNullSpi() throws KmcException {
        dao.createSa(null, (byte) 0, (short) 255, (byte) 1, (byte) 0);
        List<SecAssn> sas = dao.getSas();
        assertNotNull(sas);
        assertEquals(6, sas.size());
        SecAssn sa = sas.get(5);
        assertEquals(1, (int) sa.getSpi());
        assertEquals(1, (short) sa.getSaState());
        assertEquals(255, (short) sa.getScid());
        dao.deleteSa(sas.get(5).getId());
        sas = dao.getSas();
        assertNotNull(sas);
        assertEquals(5, sas.size());
    }

    @Test(expected = KmcException.class)
    public void testDeleteDne() throws KmcException {
        SpiScid fake = new SpiScid(10, (short) 46);
        dao.deleteSa(fake);
    }

    @Test
    public void testStartSa() throws KmcException {
        SpiScid id1 = new SpiScid(8, (short) 46);
        SpiScid id2 = new SpiScid(9, (short) 46);
        dao.createSa(8, (byte) 0, (short) 46, (byte) 10, (byte) 0);
        dao.createSa(9, (byte) 0, (short) 46, (byte) 10, (byte) 0);
        dao.startSa(id1, false);
        SecAssn sa1 = dao.getSa(id1);
        SecAssn sa2 = dao.getSa(id2);
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa1.getSaState());
        assertEquals(KmcDao.SA_UNKEYED, (short) sa2.getSaState());
    }

    @Test(expected = KmcStartException.class)
    public void testStartSaAlreadyActive() throws KmcException {
        SpiScid id2 = new SpiScid(9, (short) 46);
        testStartSa();
        dao.startSa(id2, false);
        SecAssn sa2 = dao.getSa(id2);
        assertEquals(KmcDao.SA_UNKEYED, (short) sa2.getSaState());
    }

    @Test(expected = KmcException.class)
    public void testCreateDupe() throws KmcException {
        dao.createSa(8, (byte) 0, (short) 46, (byte) 1, (byte) 0);
        dao.createSa(8, (byte) 0, (short) 46, (byte) 1, (byte) 0);

    }

    @Test(expected = KmcException.class)
    public void testStartSaDne() throws KmcException {
        SpiScid fake = new SpiScid(10, (short) 46);
        dao.startSa(fake, false);
    }

    @Test(expected = KmcException.class)
    public void testStopSaDne() throws KmcException {
        SpiScid fake = new SpiScid(10, (short) 46);
        dao.stopSa(fake);
    }

    @Test(expected = KmcException.class)
    public void testExpireSaDne() throws KmcException {
        SpiScid fake = new SpiScid(10, (short) 46);
        dao.expireSa(fake);
    }

    @Test(expected = KmcException.class)
    public void testRekeyAuthSaDne() throws KmcException {
        SpiScid fake = new SpiScid(10, (short) 46);
        dao.rekeySaAuth(fake, "0", new byte[]{0x01}, (short) 2);
    }

    @Test(expected = KmcException.class)
    public void testRekeyEncSaDne() throws KmcException {
        SpiScid fake = new SpiScid(10, (short) 46);
        dao.rekeySaEnc(fake, "130", new byte[]{0x01}, (short) 1);
    }

    @Test
    public void testStopSa() throws KmcException {
        SpiScid id1 = new SpiScid(1, (short) 46);
        SecAssn sa1 = dao.getSa(id1);
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa1.getSaState());
        dao.stopSa(id1);
        sa1 = dao.getSa(id1);
        assertEquals(KmcDao.SA_KEYED, (short) sa1.getSaState());
    }

    @Test
    public void testExpireSa() throws KmcException {
        SecAssn sa = dao.createSa(8, (byte) 0, (short) 255, (byte) 1, (byte) 0);
        dao.rekeySaEnc(sa.getId(), "130", new byte[]{0x01}, (short) 1);
        dao.rekeySaAuth(sa.getId(), "0", new byte[]{0x00}, (short) 0);
        dao.startSa(sa.getId(), false);
        sa = dao.getSa(sa.getId());
        assertEquals(KmcDao.SA_OPERATIONAL, (short) sa.getSaState());
        dao.expireSa(sa.getId());
        sa = dao.getSa(sa.getId());
        assertEquals(KmcDao.SA_UNKEYED, (short) sa.getSaState());
        assertEquals(null, sa.getAkid());
        assertEquals(null, sa.getEkid());
    }

    @Test
    public void testListSas() throws KmcException {
        List<SecAssn> sas = dao.getSas();
        assertNotNull(sas);
        assertEquals(5, sas.size());
    }

    @Test
    public void testGetSa() throws KmcException {
        SpiScid id = new SpiScid(1, (short) 46);
        SecAssn sa = dao.getSa(id);
        assertNotNull(sa);
        assertEquals(1, (int) sa.getSpi());
        assertEquals("130", sa.getEkid());
        assertEquals(null, sa.getAkid());
        assertEquals((Byte) ((byte) 0), sa.getTfvn());
    }

    @Test
    public void testGetActiveSas() throws KmcException {
        List<SecAssn> sas = dao.getActiveSas();
        assertNotNull(sas);
        assertEquals(5, sas.size());
    }

    @Test
    public void testUpdateSa() throws KmcException {
        SpiScid id = new SpiScid(1, (short) 46);
        SecAssn sa = dao.getSa(id);
        sa.setTfvn((byte) 1);
        dao.updateSa(sa);
        sa = dao.getSa(id);
        assertEquals((byte) 1, (byte) sa.getTfvn());
        assertNotNull(sa);
    }

    @Test
    public void testRollbackUpdate() throws KmcException {
        assertTrue(dao.status());
        SpiScid id = new SpiScid(1, (short) 46);
        SecAssn sa = dao.getSa(id);
        assertNotNull(sa);
        sa.setTfvn((byte) 1);
        IDbSession session = dao.newSession();
        session.beginTransaction();
        dao.updateSa(session, sa);
        sa.setTfvn((byte) 2);
        dao.updateSa(session, sa);
        session.rollback();
        sa = dao.getSa(id);
        assertEquals((byte) 0, (byte) sa.getTfvn());
    }
}