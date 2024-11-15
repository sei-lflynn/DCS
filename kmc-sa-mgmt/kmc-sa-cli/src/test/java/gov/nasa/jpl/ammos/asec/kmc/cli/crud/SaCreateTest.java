package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.FrameType;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ISecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ServiceType;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SpiScid;
import gov.nasa.jpl.ammos.asec.kmc.sadb.KmcDao;
import org.junit.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for creating an SA
 */
public class SaCreateTest extends BaseCommandLineTest {

    public static final String BULK_SA_FILE = "kmc-all-SAs.csv";

    @Test
    public void testCreateSasBulkFail() {
        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute(String.format("--file=%s", BULK_SA_FILE));
        assertNotEquals(0, exitCode);
    }

    @Test
    public void testCreateSasBulkFailDupe() throws KmcException {
        StringWriter w   = new StringWriter();
        PrintWriter  err = new PrintWriter(w);
        StringWriter o   = new StringWriter();
        PrintWriter  out = new PrintWriter(o);
        dao.createSa(20, (byte) 0, (short) 44, (byte) 20, (byte) 0, FrameType.TC);
        assertEquals(6, dao.getSas(FrameType.TC).size());
        CommandLine cmd = getCmd(new SaCreate(), true, out, err);
        int exitCode = cmd.execute(String.format("--file=%s", getClass().getClassLoader().getResource(
                BULK_SA_FILE).getFile()));

        assertTrue("Incorrect error message: " + w.toString(), w.toString().contains("SA create failed: an SA with " +
                "the SPI/SCID combination 20/44 already exists"));
    }

    @Test
    public void testCreateSasBulk() throws KmcException {
        StringWriter w   = new StringWriter();
        PrintWriter  err = new PrintWriter(w);
        StringWriter o   = new StringWriter();
        PrintWriter  out = new PrintWriter(o);
        CommandLine  cmd = getCmd(new SaCreate(), true, out, err);
        int exitCode = cmd.execute(String.format("--file=%s", getClass().getClassLoader().getResource(
                BULK_SA_FILE).getFile()));
        assertEquals(w.toString(), 0, exitCode);
        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(w.toString(), 86, sas.size());
    }


    @Test
    public void testCreateSaFail() {
        CommandLine cmd = getCmd(new SaCreate(), true, null, null);

        int exitCode = cmd.execute("--tfvn 0");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn 0", "--scid 44");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn 0", "--scid 44", "--vcid 0");
        assertNotEquals(0, exitCode);

    }

    @Test
    public void testCreateSa() throws KmcException {

        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0");
        assertEquals(0, exitCode);

        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertNotNull(sa);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7");
        assertEquals(0, exitCode);

        sa = dao.getSa(new SpiScid(7, (short) 46), FrameType.TC);
        assertNotNull(sa);
        assertEquals(KmcDao.SA_UNKEYED, (short) sa.getSaState());
    }

    @Test
    public void testCreateSaDupeFail() throws KmcException {

        CommandLine cmd = getCmd(new SaCreate(), true, null, null);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=8", "--ekid=140",
                "--ecs=0x02", "--ivlen=16", "--st=AUTHENTICATED_ENCRYPTION");
        assertEquals(0, exitCode);

        ISecAssn sa = dao.getSa(new SpiScid(8, (short) 46), FrameType.TC);
        assertNotNull(sa);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=8", "--ekid=140", "--ecs=0x02"
                , "--ivlen=16", "--st=AUTHENTICATED_ENCRYPTION");
        assertNotEquals(0, exitCode);

    }

    @Test
    public void testCreateSaShivf() throws KmcException {

        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--shivflen=20");
        assertEquals(0, exitCode);

        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertNotNull(sa);
        assertEquals(20, (short) sa.getShivfLen());
    }

    @Test
    public void testCreateSaShplf() throws KmcException {

        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--shplflen=20");
        assertEquals(0, exitCode);

        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertNotNull(sa);
        assertEquals(20, (short) sa.getShplfLen());
    }

    @Test
    public void testCreateSaShsnf() throws KmcException {

        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--shsnflen=20");
        assertEquals(0, exitCode);

        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertNotNull(sa);
        assertEquals(20, (short) sa.getShsnfLen());
    }

    @Test
    public void testCreateSaStmacf() throws KmcException {

        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--stmacflen=20");
        assertEquals(0, exitCode);

        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertNotNull(sa);
        assertEquals(20, (short) sa.getStmacfLen());
    }

    @Test
    public void createSaEkidFail() {
        CommandLine cmd = getCmd(new SaCreate(), true, null, null);

        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--ekid=130");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--ecs=0x01");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--ekid=130", "--ecs=1");
        assertNotEquals(0, exitCode);
    }

    @Test
    public void createSaEkid() throws KmcException {
        CommandLine cmd = getCmd(new SaCreate(), true, null, null);

        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--ekid=130", "--ecs" +
                "=0x01", "--st=1");
        assertEquals(0, exitCode);
        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertNotNull(sa);
        assertEquals(6, (int) sa.getId().getSpi());
        assertEquals(46, (short) sa.getId().getScid());
        assertEquals("130", sa.getEkid());
        assertArrayEquals(new byte[]{0x01}, sa.getEcs());
        assertEquals(1, (short) sa.getEcsLen());
        assertEquals(KmcDao.SA_KEYED, (short) sa.getSaState());
    }

    @Test
    public void createSaAkidFail() {
        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--akid=130");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--acs=1");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--akid=130", "--acs=1");
        assertNotEquals(0, exitCode);
    }

    @Test
    public void createSaAkid() throws KmcException {
        CommandLine cmd = getCmd(new SaCreate(), true, null, null);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--akid=130",
                "--acs=0x01", "--st=2");
        assertEquals(0, exitCode);

        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertNotNull(sa);
        assertEquals(6, (int) sa.getId().getSpi());
        assertEquals(46, (short) sa.getId().getScid());
        assertEquals("130", sa.getAkid());
        assertArrayEquals(new byte[]{0x01}, sa.getAcs());
        assertEquals(1, (short) sa.getAcsLen());
        assertEquals(KmcDao.SA_KEYED, (short) sa.getSaState());
        assertEquals(1, (short) sa.getAst());
    }

    @Test
    public void createSaIv() throws KmcException {

        CommandLine cmd = getCmd(new SaCreate(), true, null, null);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--iv" +
                "=0x000000000000000000000001", "--ivlen=12", "--st=AUTHENTICATED_ENCRYPTION");
        assertEquals(0, exitCode);
        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertArrayEquals(new byte[]{0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x01}, sa.getIv());
        assertEquals(12, (short) sa.getIvLen());
        exitCode = cmd.execute("--tfvn=0", "--scid=45", "--vcid=0", "--mapid=0", "--spi=8", "--ivlen=12", "--st" +
                "=AUTHENTICATED_ENCRYPTION");
        assertEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=8", "--ekid=140", "--ecs=0x02"
                , "--ivlen=16", "--st=AUTHENTICATED_ENCRYPTION");
        assertEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=47", "--vcid=0", "--mapid=0", "--spi=8", "--ekid=140", "--ecs=0x01"
                , "--ivlen=12", "--st=3");
        assertEquals(0, exitCode);
        exitCode = cmd.execute("--tfvn=0", "--scid=48", "--vcid=0", "--mapid=0", "--spi=8", "--ekid=140", "--ecs=0x02"
                , "--ivlen=16", "--st=AUTHENTICATED_ENCRYPTION");
        assertEquals(0, exitCode);
    }

    @Test
    public void createSaIvFail() throws KmcException {
        CommandLine cmd = getCmd(new SaCreate(), true, null, null);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--iv" +
                "=0x000000000000000000000001", "--ivlen=11", "--st=AUTHENTICATED_ENCRYPTION", "--ekid=130", "--ecs" +
                "=0x01");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--iv" +
                "=0x000000000000000000000001", "--ivlen=13", "--st=AUTHENTICATED_ENCRYPTION", "--ekid=130", "--ecs" +
                "=0x01");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--iv" +
                "=0x0000000000000000000001", "--ivlen=12");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--iv" +
                "=0x00000000000000000000001", "--ivlen=12", "--st=AUTHENTICATED_ENCRYPTION", "--ekid=130", "--ecs" +
                "=0x01");
        assertNotEquals(0, exitCode);
        //Test if the word null in IV settings are accepted
        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--iv" +
                "=null", "--ivlen=12");
        assertNotEquals(0, exitCode);
        //Test only incorrect IV len for algo
        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--st" +
                        "=AUTHENTICATED_ENCRYPTION", "--ekid=130", "--ecs=0x01",
                "--ivlen=16");

        assertNotEquals(0, exitCode);
        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(5, sas.size());
    }

    @Test
    public void createSaIvNull() throws KmcException {
        StringWriter w   = new StringWriter();
        PrintWriter  err = new PrintWriter(w);
        StringWriter o   = new StringWriter();
        PrintWriter  out = new PrintWriter(o);
        CommandLine  cmd = getCmd(new SaCreate(), true, out, err);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--ivlen=12"
                , "--ecs=0x01", "--ekid=130");
        assertEquals("Incorrect error message: " + w.toString(), 0, exitCode);
        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertNull(sa.getIv());
        assertEquals(12, (short) sa.getIvLen());
        
        /* Not yet implemented
        
        exitCode = cmd.execute("--tfvn=0", "--scid=45", "--vcid=0", "--mapid=0", "--spi=6", "--ivlen=16","--ecs=0x02");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(6, (short) 45));
        assertNull(sa.getIv());
        
        assertEquals(16,(short) sa.getIvLen());
         */
    }

    @Test
    public void createSaAbm() throws KmcException {
        StringWriter w   = new StringWriter();
        PrintWriter  err = new PrintWriter(w);
        StringWriter o   = new StringWriter();
        PrintWriter  out = new PrintWriter(o);
        CommandLine  cmd = getCmd(new SaCreate(), true, out, err);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--abm" +
                "=0x1111111111111111111111111111111111111111", "--abmlen=20");

        assertEquals("got " + w.toString(), 0, exitCode);
        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertArrayEquals(new byte[]{0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11,
                0x11}, sa.getAbm());
        assertEquals(20, (int) sa.getAbmLen());
        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(6, sas.size());
    }

    @Test
    public void createSaAbmFail() throws KmcException {
        CommandLine cmd = getCmd(new SaCreate(), true, null, null);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--abm" +
                "=0x1111111111111111111111111111111111111111", "--abmlen=21");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--abm" +
                "=0x11111111111111111111111111111111111111", "--abmlen=20");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--abm" +
                "=0x111111111111111111111111111111111111111111", "--abmlen=20");
        assertNotEquals(0, exitCode);
        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(5, sas.size());
    }

    @Test
    public void createSaArsn() throws KmcException {
        CommandLine cmd = getCmd(new SaCreate(), true, null, null);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--arsn=0x04"
                , "--arsnlen=1");
        assertEquals(0, exitCode);
        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertArrayEquals(new byte[]{0x00,
                0x00,
                (byte) 0xFC,
                0x00,
                0x00,
                (byte) 0xFF,
                (byte) 0xFF,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00}, sa.getAbm());
        assertEquals(19, (int) sa.getAbmLen());
        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(6, sas.size());
    }

    @Test
    public void createSaArsnFail() {
        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--arsn=0x04");
        assertNotEquals(0, exitCode);
    }

    @Test
    public void createSaArsnw() throws KmcException {
        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--arsnw=5");
        assertEquals(0, exitCode);
        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertEquals(5, (short) sa.getArsnw());
        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(6, sas.size());
    }

    @Test
    public void createSaEkidAkid() throws KmcException {
        StringWriter w   = new StringWriter();
        PrintWriter  err = new PrintWriter(w);
        StringWriter o   = new StringWriter();
        PrintWriter  out = new PrintWriter(o);
        CommandLine  cmd = getCmd(new SaCreate(), true, out, err);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7", "--ekid=130",
                "--ecs=0x01", "--akid=130", "--acs=0x01", "--st=3");
        assertEquals("got " + w.toString(), 0, exitCode);
        ISecAssn sa = dao.getSa(new SpiScid(7, (short) 46), FrameType.TC);
        assertNotNull(sa);
        assertEquals(7, (int) sa.getSpi());
        assertEquals(0, (short) sa.getTfvn());
        assertEquals(1, (short) sa.getAst());
        assertEquals(1, (short) sa.getEst());
        assertEquals(0, (byte) sa.getVcid());
        assertEquals(0, (short) sa.getMapid());
        assertArrayEquals(new byte[]{0x01}, sa.getEcs());
        assertArrayEquals(new byte[]{0x01}, sa.getAcs());
        assertEquals("130", sa.getEkid());
        assertEquals("130", sa.getAkid());
    }

    @Test
    public void createSaStFail() throws KmcException {
        CommandLine cmd      = getCmd(new SaCreate(), true, null, null);
        int         exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--st=-1");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7", "--st=4");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7", "--ekid=130", "--ecs=0x01"
                , "--st=4");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7", "--ekid=130", "--ecs=0x01"
                , "--st=-1");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7", "--akid=130", "--acs=0x01"
                , "--st=4");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7", "--akid=130", "--acs=0x01"
                , "--st=-1");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7", "--akid=130", "--acs=0x01"
                , "--st=HI");
        assertNotEquals(0, exitCode);

        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(5, sas.size());
    }

    @Test
    public void createSaSt() throws KmcException {
        CommandLine cmd = getCmd(new SaCreate(), true, null, null);
        int exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=6", "--st" +
                "=ENCRYPTION");
        assertEquals(0, exitCode);
        ISecAssn sa = dao.getSa(new SpiScid(6, (short) 46), FrameType.TC);
        assertEquals(ServiceType.ENCRYPTION, sa.getServiceType());
        assertEquals(1, (short) sa.getEst());
        assertEquals(0, (short) sa.getAst());

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=7", "--st" +
                "=1");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(7, (short) 46), FrameType.TC);
        assertEquals(ServiceType.ENCRYPTION, sa.getServiceType());
        assertEquals(1, (short) sa.getEst());
        assertEquals(0, (short) sa.getAst());

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=8", "--st" +
                "=AUTHENTICATION");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(8, (short) 46), FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATION, sa.getServiceType());
        assertEquals(0, (short) sa.getEst());
        assertEquals(1, (short) sa.getAst());

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=9", "--st" +
                "=2");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(9, (short) 46), FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATION, sa.getServiceType());
        assertEquals(0, (short) sa.getEst());
        assertEquals(1, (short) sa.getAst());

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=10", "--st" +
                "=AUTHENTICATED_ENCRYPTION");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(10, (short) 46), FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATED_ENCRYPTION, sa.getServiceType());
        assertEquals(1, (short) sa.getEst());
        assertEquals(1, (short) sa.getAst());

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=11", "--st" +
                "=3");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(11, (short) 46), FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATED_ENCRYPTION, sa.getServiceType());
        assertEquals(1, (short) sa.getEst());
        assertEquals(1, (short) sa.getAst());

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=12", "--st" +
                "=PLAINTEXT");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(12, (short) 46), FrameType.TC);
        assertEquals(ServiceType.PLAINTEXT, sa.getServiceType());
        assertEquals(0, (short) sa.getEst());
        assertEquals(0, (short) sa.getAst());

        exitCode = cmd.execute("--tfvn=0", "--scid=46", "--vcid=0", "--mapid=0", "--spi=13", "--st" +
                "=0");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(13, (short) 46), FrameType.TC);
        assertEquals(ServiceType.PLAINTEXT, sa.getServiceType());
        assertEquals(0, (short) sa.getEst());
        assertEquals(0, (short) sa.getAst());
    }

}