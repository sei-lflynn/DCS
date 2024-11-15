package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.FrameType;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ISecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ServiceType;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SpiScid;
import org.junit.Test;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for updating an SA
 */
public class SaUpdateTest extends BaseCommandLineTest {

    public static final String BULK_SA_FILE  = "kmc-all-SAs.csv";
    public static final String H2_SA_UPDATES = "test-sas-h2.csv";

    @Test
    public void testUpdateBulkNotExist() throws KmcException {
        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(5, sas.size());
        CommandLine cli = getCmd(new SaUpdate(), true);
        int exit = cli.execute(String.format("--file=%s", getClass().getClassLoader().getResource(
                BULK_SA_FILE).getFile()));
        assertEquals(0, exit);
        sas = dao.getSas(FrameType.TC);
        assertEquals(5, sas.size());
    }

    @Test
    public void testUpdateBulk() throws KmcException {
        List<? extends ISecAssn> sas = dao.getSas(FrameType.TC);
        assertEquals(5, sas.size());
        assertEquals(1, (int) sas.get(0).getSpi());
        CommandLine cli = getCmd(new SaUpdate(), true);
        int exit = cli.execute(String.format("--file=%s", getClass().getClassLoader().getResource(
                H2_SA_UPDATES).getFile()));
        assertEquals(0, exit);
        sas = dao.getSas(FrameType.TC);
        assertEquals(5, sas.size());
        sas.sort(Comparator.comparing(ISecAssn::getSpi));
        assertEquals(ServiceType.ENCRYPTION, sas.get(0).getServiceType());
        assertEquals("140", sas.get(0).getEkid());
        assertEquals(ServiceType.AUTHENTICATION, sas.get(1).getServiceType());
        assertEquals("140", sas.get(1).getEkid());
        assertEquals(ServiceType.ENCRYPTION, sas.get(2).getServiceType());
        assertEquals("140", sas.get(2).getEkid());
        assertEquals(ServiceType.AUTHENTICATED_ENCRYPTION, sas.get(3).getServiceType());
        assertEquals("140", sas.get(3).getEkid());
        assertEquals(ServiceType.AUTHENTICATED_ENCRYPTION, sas.get(4).getServiceType());
        assertEquals("140", sas.get(4).getEkid());
    }

    @Test
    public void testNoArgsFail() {
        CommandLine cli  = getCmd(new SaUpdate(), true);
        int         exit = cli.execute();
        assertNotEquals(0, exit);
    }

    @Test
    public void testResetArsnFail() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(5, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(4, (short) sa.getArsnLen());
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x01}, sa.getArsn());

        int exit = cli.execute("--spi=5", "--scid=46", "--arsn=0x01");
        assertNotEquals(0, exit);

        exit = cli.execute("--spi=5", "--scid=46", "--arsnlen=6");
        assertNotEquals(0, exit);
    }

    @Test
    public void testResetArsn() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(5, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(4, (short) sa.getArsnLen());
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x01}, sa.getArsn());
        int exit = cli.execute("--spi=5", "--scid=46", "--arsn=0x0000000002", "--arsnlen=5");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals(5, (short) sa.getArsnLen());
        assertArrayEquals(new byte[]{0x00, 0x00, 0x00, 0x00, 0x02}, sa.getArsn());
    }

    @Test
    public void testResetIv() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(0, (short) sa.getIvLen());
        assertNull(sa.getIv());
        int exit = cli.execute("--spi=1", "--scid=46", "--iv=0x00112233465566778899aabbccddeeff", "--ivlen=16");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals(16, (short) sa.getIvLen());
        assertArrayEquals(new byte[]{0x00,
                0x11,
                0x22,
                0x33,
                0x46,
                0x55,
                0x66,
                0x77,
                (byte) 0x88,
                (byte) 0x99,
                (byte) 0xaa,
                (byte) 0xbb,
                (byte) 0xcc,
                (byte) 0xdd,
                (byte) 0xee,
                (byte) 0xff}, sa.getIv());
    }

    @Test
    public void testResetIvFail() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(0, (short) sa.getIvLen());
        assertNull(sa.getIv());

        int exit = cli.execute("--spi=1", "--scid=46", "--ecs=0x01", "--iv=0x00112233465566778899aabb", "--ivlen=12");
        assertNotEquals(0, exit);

        exit = cli.execute("--spi=1", "--scid=46", "--ecs=0x01", "--iv=0x01");
        assertNotEquals(0, exit);

        exit = cli.execute("--spi=1", "--scid=46", "--ivlen=8", "--ecs=0x01", "--ekid=130");
        assertNotEquals(0, exit);
    }

    @Test
    public void testResetShivf() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(12, (short) sa.getShivfLen());
        int exit = cli.execute("--spi=1", "--scid=46", "--shivflen=20");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals(20, (short) sa.getShivfLen());
    }

    @Test
    public void testResetShplf() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(0, (short) sa.getShplfLen());
        int exit = cli.execute("--spi=1", "--scid=46", "--shplflen=20");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals(20, (short) sa.getShplfLen());
    }

    @Test
    public void testResetShsnf() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(0, (short) sa.getShsnfLen());
        int exit = cli.execute("--spi=1", "--scid=46", "--shsnflen=20");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals(20, (short) sa.getShsnfLen());
    }

    @Test
    public void testResetStmacf() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(16, (short) sa.getStmacfLen());
        int exit = cli.execute("--spi=1", "--scid=46", "--stmacflen=20");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals(20, (short) sa.getStmacfLen());
    }

    @Test
    public void testResetEkidFail() throws KmcException {
        CommandLine cmd = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals("130", sa.getEkid());
        int exitCode = cmd.execute("--spi=1", "--scid=46", "--ekid=140");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--scid=46", "--spi=1", "--ecs=0x01");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--scid=46", "--spi=1", "--ekid=130", "--ecs=1");
        assertNotEquals(0, exitCode);
    }

    @Test
    public void testResetEkid() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals("130", sa.getEkid());
        assertArrayEquals(new byte[]{0x01}, sa.getEcs());
        int exit = cli.execute("--spi=1", "--scid=46", "--ekid=140", "--ecs=0x02", "--ivlen=16");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals("140", sa.getEkid());
        assertArrayEquals(new byte[]{0x02}, sa.getEcs());
    }

    @Test
    public void testResetAkidFail() throws KmcException {
        CommandLine cmd = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertNull(sa.getAkid());
        assertArrayEquals(new byte[]{0x00}, sa.getAcs());
        int exitCode = cmd.execute("--spi=1", "--scid=46", "--akid=140");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--scid=46", "--spi=1", "--acs=0x01");
        assertNotEquals(0, exitCode);

        exitCode = cmd.execute("--scid=46", "--spi=1", "--akid=130", "--acs=1");
        assertNotEquals(0, exitCode);
    }

    @Test
    public void testResetAkid() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertNull(sa.getAkid());
        assertArrayEquals(new byte[]{0x00}, sa.getAcs());
        int exit = cli.execute("--spi=1", "--scid=46", "--akid=140", "--acs=0x02");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals("140", sa.getAkid());
        assertArrayEquals(new byte[]{0x02}, sa.getAcs());
    }

    @Test
    public void testResetAbm() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(19, (int) sa.getAbmLen());
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
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00,
                0x00}, sa.getAbm());
        int exit = cli.execute("--spi=1", "--scid=46", "--abm=0x1111111111111111111111111111111111111111",
                "--abmlen" + "=20");
        assertEquals(0, exit);
        sa = dao.getSa(id, FrameType.TC);
        assertEquals(20, (int) sa.getAbmLen());
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
    }

    @Test
    public void testResetAbmFail() throws KmcException {
        CommandLine cli = getCmd(new SaUpdate(), true);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertNull(sa.getAkid());
        assertArrayEquals(new byte[]{0x00}, sa.getAcs());
        int exit = cli.execute("--spi=1", "--scid=46", "--abm=0x1111111111111111111111111111111111111111",
                "--abmlen" + "=21");
        assertNotEquals(0, exit);

        exit = cli.execute("--spi=1", "--scid=46", "--abm=0x11111111111111111111111111111111111111", "--abmlen=20");
        assertNotEquals(0, exit);

        exit = cli.execute("--spi=1", "--scid=46", "--abm=111111111111111111111111111111111111111111", "--abmlen=20");
        assertNotEquals(0, exit);
    }

    @Test
    public void testResetArsnw() throws KmcException {
        CommandLine cmd = getCmd(new SaUpdate(), true, null, null);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(5, (short) sa.getArsnw());
        int exitCode = cmd.execute("--scid=46", "--spi=1", "--arsnw=10");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(10, (short) sa.getArsnw());
    }

    @Test
    public void testResetSt() throws KmcException {
        CommandLine cmd = getCmd(new SaUpdate(), true, null, null);
        SpiScid     id  = new SpiScid(1, (short) 46);
        ISecAssn    sa  = dao.getSa(id, FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATED_ENCRYPTION, sa.getServiceType());
        int exitCode = cmd.execute("--scid=46", "--spi=1", "--st=1");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(ServiceType.ENCRYPTION, sa.getServiceType());

        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=2");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATION, sa.getServiceType());

        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=0");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(ServiceType.PLAINTEXT, sa.getServiceType());

        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=3");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATED_ENCRYPTION, sa.getServiceType());

        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=ENCRYPTION");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(ServiceType.ENCRYPTION, sa.getServiceType());

        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=PLAINTEXT");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(ServiceType.PLAINTEXT, sa.getServiceType());

        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=AUTHENTICATION");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATION, sa.getServiceType());

        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=AUTHENTICATED_ENCRYPTION");
        assertEquals(0, exitCode);
        sa = dao.getSa(new SpiScid(1, (short) 46), FrameType.TC);
        assertEquals(ServiceType.AUTHENTICATED_ENCRYPTION, sa.getServiceType());
    }

    @Test
    public void testResetStFail() {
        CommandLine cmd      = getCmd(new SaUpdate(), true, null, null);
        int         exitCode = cmd.execute("--scid=46", "--spi=1", "--st=4");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=-1");
        assertNotEquals(0, exitCode);
        exitCode = cmd.execute("--scid=46", "--spi=1", "--st=unknown");
        assertNotEquals(0, exitCode);
    }

}