package gov.nasa.jpl.ammos.asec.kmc.sadb;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BaseH2Test {
    public static KmcDao dao;

    @BeforeClass
    public static void beforeClass() throws KmcException {
        dao = new KmcDao("sadb_user", "sadb_test");
        dao.init();
        System.setProperty("KMC_UNIT_TEST", "true");
    }

    /**
     * Before each test, populate the sample DB
     *
     * @throws SQLException
     * @throws IOException
     */
    @Before
    public void beforeTest() throws SQLException {
        setupTable("/create_sadb_jpl_unit_test_security_associations.sql");
        setupTable("/create_sadb_jpl_unit_test_security_associations_aos.sql");
        setupTable("/create_sadb_jpl_unit_test_security_associations_tm.sql");
    }

    private void setupTable(String sqlFile) {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sadb_user", "sadb_test");
             Reader reader = new InputStreamReader(getClass().getResourceAsStream(sqlFile))) {
            RunScript.execute(conn, reader);
        } catch (SQLException sqlException) {
            throw new RuntimeException("Encountered unexpected SQLException while setting up unit test DB: ",
                    sqlException);
        } catch (IOException ioException) {
            throw new RuntimeException("Encountered unexpected IOException while setting up unit test DB: ",
                    ioException);
        }
    }

    /**
     * After each test, truncate the sample DB
     *
     * @throws SQLException
     */
    @After
    public void afterTest() throws SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:test", "sadb_user", "sadb_test")) {
            conn.createStatement().execute("TRUNCATE TABLE sadb.security_associations");
            conn.createStatement().execute("TRUNCATE TABLE sadb.security_associations_aos");
            conn.createStatement().execute("TRUNCATE TABLE sadb.security_associations_tm");
        }

    }
}
