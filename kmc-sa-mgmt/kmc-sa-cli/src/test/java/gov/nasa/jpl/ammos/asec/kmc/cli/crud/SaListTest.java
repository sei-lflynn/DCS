package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.FrameType;
import org.junit.Test;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests for listing SAs
 */
public class SaListTest extends BaseCommandLineTest {

    @Test
    public void listFilterSpi() {
        StringWriter w    = new StringWriter();
        PrintWriter  out  = new PrintWriter(w);
        CommandLine  cli  = getCmd(new SaList(), true, out, null);
        int          exit = cli.execute("--spi=1");
        assertEquals(0, exit);
        assertEquals("\"spi\",\"scid\",\"vcid\",\"tfvn\",\"mapid\",\"sa_state\",\"ekid\",\"est\",\"akid\",\"ast\"\n" +
                "\"1\",\"46\",\"0\",\"0\",\"0\",\"3\",\"130\",\"1\",\"\",\"1\"\n", w.toString());
    }

    @Test
    public void listFilterScid() {
        StringWriter w    = new StringWriter();
        PrintWriter  out  = new PrintWriter(w);
        CommandLine  cli  = getCmd(new SaList(), true, out, null);
        int          exit = cli.execute("--scid=1");
        assertEquals(0, exit);
        assertEquals("\"spi\",\"scid\",\"vcid\",\"tfvn\",\"mapid\",\"sa_state\",\"ekid\",\"est\",\"akid\"," +
                "\"ast\"\n", w.toString());
    }

    @Test
    public void listFilterSpiScid() {
        StringWriter w    = new StringWriter();
        PrintWriter  out  = new PrintWriter(w);
        CommandLine  cli  = getCmd(new SaList(), true, out, null);
        int          exit = cli.execute("--spi=2", "--scid=46");
        assertEquals(0, exit);
        assertEquals("\"spi\",\"scid\",\"vcid\",\"tfvn\",\"mapid\",\"sa_state\",\"ekid\",\"est\",\"akid\",\"ast\"\n" +
                "\"2\",\"46\",\"1\",\"0\",\"0\",\"3\",\"130\",\"1\",\"\",\"1\"\n", w.toString());
    }

    @Test
    public void testActive() throws KmcException {
        StringWriter w   = new StringWriter();
        PrintWriter  out = new PrintWriter(w);
        dao.createSa(6, (byte) 0, (short) 44, (byte) 0, (byte) 0, FrameType.TC);
        CommandLine cli  = getCmd(new SaList(), true, out, null);
        int         exit = cli.execute("--active");
        assertEquals(0, exit);
        assertEquals("\"spi\",\"scid\",\"vcid\",\"tfvn\",\"mapid\",\"sa_state\",\"ekid\",\"est\",\"akid\",\"ast\"\n" +
                "\"1\",\"46\",\"0\",\"0\",\"0\",\"3\",\"130\",\"1\",\"\",\"1\"\n" +
                "\"2\",\"46\",\"1\",\"0\",\"0\",\"3\",\"130\",\"1\",\"\",\"1\"\n" +
                "\"3\",\"46\",\"2\",\"0\",\"0\",\"3\",\"130\",\"1\",\"\",\"1\"\n" +
                "\"4\",\"46\",\"3\",\"0\",\"0\",\"3\",\"130\",\"0\",\"\",\"1\"\n" +
                "\"5\",\"46\",\"7\",\"0\",\"0\",\"3\",\"\",\"0\",\"130\",\"1\"\n", w.toString());
    }

    @Test
    public void testInactive() throws KmcException {
        StringWriter w   = new StringWriter();
        PrintWriter  out = new PrintWriter(w);
        dao.createSa(6, (byte) 0, (short) 46, (byte) 0, (byte) 0, FrameType.TC);
        CommandLine cli  = getCmd(new SaList(), true, out, null);
        int         exit = cli.execute("--inactive");
        assertEquals(0, exit);
        assertEquals("\"spi\",\"scid\",\"vcid\",\"tfvn\",\"mapid\",\"sa_state\",\"ekid\",\"est\",\"akid\",\"ast\"\n" +
                "\"6\",\"46\",\"0\",\"0\",\"0\",\"1\",\"\",\"0\",\"\",\"0\"\n", w.toString());
    }

    @Test
    public void listFail() {
        CommandLine cli  = getCmd(new SaList(), true);
        int         exit = cli.execute("-e", "--mysql");
        assertNotEquals(0, exit);

        exit = cli.execute("-e", "--json");
        assertNotEquals(0, exit);
    }

    @Test
    public void list() {
        StringWriter w    = new StringWriter();
        PrintWriter  out  = new PrintWriter(w);
        CommandLine  cli  = getCmd(new SaList(), true, out, null);
        int          exit = cli.execute();
        assertEquals(0, exit);
        assertEquals("\"spi\",\"scid\",\"vcid\",\"tfvn\",\"mapid\",\"sa_state\",\"ekid\"," +
                "\"est\",\"akid\",\"ast\"\n\"1\",\"46\",\"0\",\"0\",\"0\",\"3\",\"130\",\"1\",\"\",\"1\"\n" +
                "\"2\",\"46\",\"1\",\"0\",\"0\",\"3\",\"130\",\"1\",\"\",\"1\"\n\"3\",\"46\",\"2\",\"0\",\"0\"," +
                "\"3\",\"130\",\"1\",\"\",\"1\"\n\"4\",\"46\",\"3\",\"0\",\"0\",\"3\",\"130\",\"0\",\"\"," +
                "\"1\"\n\"5\",\"46\",\"7\",\"0\",\"0\",\"3\",\"\",\"0\",\"130\",\"1\"\n", w.toString());
    }

    @Test
    public void listExtended() {
        StringWriter w    = new StringWriter();
        PrintWriter  out  = new PrintWriter(w);
        CommandLine  cli  = getCmd(new SaList(), true, out, null);
        int          exit = cli.execute("--extended");
        assertEquals(0, exit);
        assertEquals("\"spi\",\"scid\",\"vcid\",\"tfvn\",\"mapid\",\"sa_state\",\"st\",\"shivf_len\",\"shsnf_len\"," +
                "\"shplf_len\",\"stmacf_len\",\"ecs\",\"ekid\",\"iv_len\",\"iv\",\"acs\",\"akid\",\"abm_len\"," +
                "\"abm\",\"arsn_len\",\"arsn\",\"arsnw\"\n\"1\",\"46\",\"0\",\"0\",\"0\",\"3\"," +
                "\"AUTHENTICATED_ENCRYPTION\",\"12\",\"0\",\"0\",\"16\",\"0x01\",\"130\",\"0\"," +
                "\"\",\"0x00\",\"\",\"19\",\"0x00000000000000000000000000000000000000\"," +
                "\"0\",\"0x0000000000000000000000000000000000000000\",\"5\"\n\"2\",\"46\",\"1\",\"0\",\"0\",\"3\"," +
                "\"AUTHENTICATED_ENCRYPTION\",\"12\",\"0\",\"0\",\"16\",\"0x01\",\"130\",\"12\"," +
                "\"0x000000000000000000000001\",\"0x00\",\"\",\"19\",\"0x00000000000000000000000000000000000000\"," +
                "\"0\",\"0x0000000000000000000000000000000000000000\",\"5\"\n\"3\",\"46\",\"2\",\"0\",\"0\",\"3\"," +
                "\"AUTHENTICATED_ENCRYPTION\",\"12\",\"0\",\"0\",\"16\",\"0x01\",\"130\",\"12\"," +
                "\"0x000000000000000000000001\",\"0x00\",\"\",\"19\",\"0x00000000000000000000000000000000000000\"," +
                "\"0\",\"0x0000000000000000000000000000000000000000\",\"5\"\n\"4\",\"46\",\"3\",\"0\",\"0\",\"3\"," +
                "\"AUTHENTICATION\",\"12\",\"0\",\"0\",\"16\",\"0x01\",\"130\",\"12\",\"0x000000000000000000000001\"," +
                "\"0x00\",\"\",\"1024\"," +
                "\"0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffff\",\"0\",\"0x0000000000000000000000000000000000000000\",\"5\"\n\"5\",\"46\",\"7\",\"0\"," +
                "\"0\",\"3\",\"AUTHENTICATION\",\"0\",\"4\",\"0\",\"16\",\"0x00\",\"\",\"0\"," +
                "\"\",\"0x01\",\"130\",\"1024\"," +
                "\"0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffff\",\"4\",\"0x00000001\",\"5\"\n", w.toString());
    }

    @Test
    public void listMysql() {
        StringWriter w    = new StringWriter();
        PrintWriter  out  = new PrintWriter(w);
        CommandLine  cli  = getCmd(new SaList(), true, out, null);
        int          exit = cli.execute("--mysql");
        assertEquals(0, exit);
        assertEquals("*************************** 1. row ***************************\n" +
                "       spi: 1\n" +
                "      ekid: 130\n" +
                "      akid: \n" +
                "  sa_state: 3\n" +
                "      tfvn: 0\n" +
                "      scid: 46\n" +
                "      vcid: 0\n" +
                "     mapid: 0\n" +
                "      lpid: \n" +
                "        st: AUTHENTICATED_ENCRYPTION\n" +
                " shivf_len: 12\n" +
                " shsnf_len: 0\n" +
                " shplf_len: 0\n" +
                "stmacf_len: 16\n" +
                "   ecs_len: 1\n" +
                "       ecs: 0x01\n" +
                "    iv_len: 0\n" +
                "        iv: \n" +
                "   acs_len: 0\n" +
                "       acs: 0x00\n" +
                "   abm_len: 19\n" +
                "       abm: 0x00000000000000000000000000000000000000\n" +
                "  arsn_len: 0\n" +
                "      arsn: 0x0000000000000000000000000000000000000000\n" +
                "     arsnw: 5\n" +
                "*************************** 2. row ***************************\n" +
                "       spi: 2\n" +
                "      ekid: 130\n" +
                "      akid: \n" +
                "  sa_state: 3\n" +
                "      tfvn: 0\n" +
                "      scid: 46\n" +
                "      vcid: 1\n" +
                "     mapid: 0\n" +
                "      lpid: \n" +
                "        st: AUTHENTICATED_ENCRYPTION\n" +
                " shivf_len: 12\n" +
                " shsnf_len: 0\n" +
                " shplf_len: 0\n" +
                "stmacf_len: 16\n" +
                "   ecs_len: 1\n" +
                "       ecs: 0x01\n" +
                "    iv_len: 12\n" +
                "        iv: 0x000000000000000000000001\n" +
                "   acs_len: 0\n" +
                "       acs: 0x00\n" +
                "   abm_len: 19\n" +
                "       abm: 0x00000000000000000000000000000000000000\n" +
                "  arsn_len: 0\n" +
                "      arsn: 0x0000000000000000000000000000000000000000\n" +
                "     arsnw: 5\n" +
                "*************************** 3. row ***************************\n" +
                "       spi: 3\n" +
                "      ekid: 130\n" +
                "      akid: \n" +
                "  sa_state: 3\n" +
                "      tfvn: 0\n" +
                "      scid: 46\n" +
                "      vcid: 2\n" +
                "     mapid: 0\n" +
                "      lpid: \n" +
                "        st: AUTHENTICATED_ENCRYPTION\n" +
                " shivf_len: 12\n" +
                " shsnf_len: 0\n" +
                " shplf_len: 0\n" +
                "stmacf_len: 16\n" +
                "   ecs_len: 1\n" +
                "       ecs: 0x01\n" +
                "    iv_len: 12\n" +
                "        iv: 0x000000000000000000000001\n" +
                "   acs_len: 0\n" +
                "       acs: 0x00\n" +
                "   abm_len: 19\n" +
                "       abm: 0x00000000000000000000000000000000000000\n" +
                "  arsn_len: 0\n" +
                "      arsn: 0x0000000000000000000000000000000000000000\n" +
                "     arsnw: 5\n" +
                "*************************** 4. row ***************************\n" +
                "       spi: 4\n" +
                "      ekid: 130\n" +
                "      akid: \n" +
                "  sa_state: 3\n" +
                "      tfvn: 0\n" +
                "      scid: 46\n" +
                "      vcid: 3\n" +
                "     mapid: 0\n" +
                "      lpid: \n" +
                "        st: AUTHENTICATION\n" +
                " shivf_len: 12\n" +
                " shsnf_len: 0\n" +
                " shplf_len: 0\n" +
                "stmacf_len: 16\n" +
                "   ecs_len: 1\n" +
                "       ecs: 0x01\n" +
                "    iv_len: 12\n" +
                "        iv: 0x000000000000000000000001\n" +
                "   acs_len: 0\n" +
                "       acs: 0x00\n" +
                "   abm_len: 1024\n" +
                "       abm: " +
                "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffff\n" +
                "  arsn_len: 0\n" +
                "      arsn: 0x0000000000000000000000000000000000000000\n" +
                "     arsnw: 5\n" +
                "*************************** 5. row ***************************\n" +
                "       spi: 5\n" +
                "      ekid: \n" +
                "      akid: 130\n" +
                "  sa_state: 3\n" +
                "      tfvn: 0\n" +
                "      scid: 46\n" +
                "      vcid: 7\n" +
                "     mapid: 0\n" +
                "      lpid: \n" +
                "        st: AUTHENTICATION\n" +
                " shivf_len: 0\n" +
                " shsnf_len: 4\n" +
                " shplf_len: 0\n" +
                "stmacf_len: 16\n" +
                "   ecs_len: 1\n" +
                "       ecs: 0x00\n" +
                "    iv_len: 0\n" +
                "        iv: \n" +
                "   acs_len: 0\n" +
                "       acs: 0x01\n" +
                "   abm_len: 1024\n" +
                "       abm: " +
                "0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffff\n" +
                "  arsn_len: 4\n" +
                "      arsn: 0x00000001\n" +
                "     arsnw: 5\n", w.toString());
    }

    @Test
    public void testJson() {
        StringWriter w    = new StringWriter();
        PrintWriter  out  = new PrintWriter(w);
        CommandLine  cli  = getCmd(new SaList(), true, out, null);
        int          exit = cli.execute("--json");
        assertEquals(0, exit);
        assertEquals("{\n" +
                "    {\n" +
                "        \"spi\": 1,\n" +
                "        \"ekid\": \"130\",\n" +
                "        \"akid\": \"\",\n" +
                "        \"sa_state\": 3,\n" +
                "        \"tfvn\": 0,\n" +
                "        \"scid\": 46,\n" +
                "        \"vcid\": 0,\n" +
                "        \"mapid\": 0,\n" +
                "        \"st\": \"AUTHENTICATED_ENCRYPTION\",\n" +
                "        \"shivf_len\": 12,\n" +
                "        \"shsnf_len\": 0,\n" +
                "        \"shplf_len\": 0,\n" +
                "        \"stmacf_len\": 16,\n" +
                "        \"ecs_len\": 1,\n" +
                "        \"ecs\": \"0x01\",\n" +
                "        \"iv_len\": 0,\n" +
                "        \"iv\": \"\",\n" +
                "        \"acs_len\": 0,\n" +
                "        \"acs\": \"0x00\",\n" +
                "        \"abm_len\": 19,\n" +
                "        \"abm\": \"0x00000000000000000000000000000000000000\",\n" +
                "        \"arsn_len\": 0,\n" +
                "        \"arsn\": \"0x0000000000000000000000000000000000000000\",\n" +
                "        \"arsnw\": 5\n" +
                "    }\n" +
                "    {\n" +
                "        \"spi\": 2,\n" +
                "        \"ekid\": \"130\",\n" +
                "        \"akid\": \"\",\n" +
                "        \"sa_state\": 3,\n" +
                "        \"tfvn\": 0,\n" +
                "        \"scid\": 46,\n" +
                "        \"vcid\": 1,\n" +
                "        \"mapid\": 0,\n" +
                "        \"st\": \"AUTHENTICATED_ENCRYPTION\",\n" +
                "        \"shivf_len\": 12,\n" +
                "        \"shsnf_len\": 0,\n" +
                "        \"shplf_len\": 0,\n" +
                "        \"stmacf_len\": 16,\n" +
                "        \"ecs_len\": 1,\n" +
                "        \"ecs\": \"0x01\",\n" +
                "        \"iv_len\": 12,\n" +
                "        \"iv\": \"0x000000000000000000000001\",\n" +
                "        \"acs_len\": 0,\n" +
                "        \"acs\": \"0x00\",\n" +
                "        \"abm_len\": 19,\n" +
                "        \"abm\": \"0x00000000000000000000000000000000000000\",\n" +
                "        \"arsn_len\": 0,\n" +
                "        \"arsn\": \"0x0000000000000000000000000000000000000000\",\n" +
                "        \"arsnw\": 5\n" +
                "    }\n" +
                "    {\n" +
                "        \"spi\": 3,\n" +
                "        \"ekid\": \"130\",\n" +
                "        \"akid\": \"\",\n" +
                "        \"sa_state\": 3,\n" +
                "        \"tfvn\": 0,\n" +
                "        \"scid\": 46,\n" +
                "        \"vcid\": 2,\n" +
                "        \"mapid\": 0,\n" +
                "        \"st\": \"AUTHENTICATED_ENCRYPTION\",\n" +
                "        \"shivf_len\": 12,\n" +
                "        \"shsnf_len\": 0,\n" +
                "        \"shplf_len\": 0,\n" +
                "        \"stmacf_len\": 16,\n" +
                "        \"ecs_len\": 1,\n" +
                "        \"ecs\": \"0x01\",\n" +
                "        \"iv_len\": 12,\n" +
                "        \"iv\": \"0x000000000000000000000001\",\n" +
                "        \"acs_len\": 0,\n" +
                "        \"acs\": \"0x00\",\n" +
                "        \"abm_len\": 19,\n" +
                "        \"abm\": \"0x00000000000000000000000000000000000000\",\n" +
                "        \"arsn_len\": 0,\n" +
                "        \"arsn\": \"0x0000000000000000000000000000000000000000\",\n" +
                "        \"arsnw\": 5\n" +
                "    }\n" +
                "    {\n" +
                "        \"spi\": 4,\n" +
                "        \"ekid\": \"130\",\n" +
                "        \"akid\": \"\",\n" +
                "        \"sa_state\": 3,\n" +
                "        \"tfvn\": 0,\n" +
                "        \"scid\": 46,\n" +
                "        \"vcid\": 3,\n" +
                "        \"mapid\": 0,\n" +
                "        \"st\": \"AUTHENTICATION\",\n" +
                "        \"shivf_len\": 12,\n" +
                "        \"shsnf_len\": 0,\n" +
                "        \"shplf_len\": 0,\n" +
                "        \"stmacf_len\": 16,\n" +
                "        \"ecs_len\": 1,\n" +
                "        \"ecs\": \"0x01\",\n" +
                "        \"iv_len\": 12,\n" +
                "        \"iv\": \"0x000000000000000000000001\",\n" +
                "        \"acs_len\": 0,\n" +
                "        \"acs\": \"0x00\",\n" +
                "        \"abm_len\": 1024,\n" +
                "        \"abm\": " +
                "\"0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffff\",\n" +
                "        \"arsn_len\": 0,\n" +
                "        \"arsn\": \"0x0000000000000000000000000000000000000000\",\n" +
                "        \"arsnw\": 5\n" +
                "    }\n" +
                "    {\n" +
                "        \"spi\": 5,\n" +
                "        \"ekid\": \"\",\n" +
                "        \"akid\": \"130\",\n" +
                "        \"sa_state\": 3,\n" +
                "        \"tfvn\": 0,\n" +
                "        \"scid\": 46,\n" +
                "        \"vcid\": 7,\n" +
                "        \"mapid\": 0,\n" +
                "        \"st\": \"AUTHENTICATION\",\n" +
                "        \"shivf_len\": 0,\n" +
                "        \"shsnf_len\": 4,\n" +
                "        \"shplf_len\": 0,\n" +
                "        \"stmacf_len\": 16,\n" +
                "        \"ecs_len\": 1,\n" +
                "        \"ecs\": \"0x00\",\n" +
                "        \"iv_len\": 0,\n" +
                "        \"iv\": \"\",\n" +
                "        \"acs_len\": 0,\n" +
                "        \"acs\": \"0x01\",\n" +
                "        \"abm_len\": 1024,\n" +
                "        \"abm\": " +
                "\"0xffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffff\",\n" +
                "        \"arsn_len\": 4,\n" +
                "        \"arsn\": \"0x00000001\",\n" +
                "        \"arsnw\": 5\n" +
                "    }\n" +
                "}\n", w.toString());
    }
}