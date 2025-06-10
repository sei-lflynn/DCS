package gov.nasa.jpl.ammos.kmc.crypto.client.test;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManager;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManagerException;

/**
 * Utility class for creating KMIP keys.
 *
 *
 */
public final class CryptoTestUtils {

    private static final String ADMIN_NAME = "testuser3300";
    private static final String USER_NAME = "testuser3000";
    private static final String PASSWORD = "let8me2in";

    private static String[] adminArgument = new String[] {
        "-username=" + ADMIN_NAME, "-password=" + PASSWORD
    };
    private static String[] userArgument = new String[] {
        "-username=" + USER_NAME, "-password=" + PASSWORD
    };

    private static SecureRandom random = null;
    private static final Logger logger = LoggerFactory.getLogger(CryptoTestUtils.class);

    private CryptoTestUtils() { }

    public static synchronized byte[] createTestData(final int nBytes) {
        if (random == null) {
            try {
                random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            } catch (NoSuchAlgorithmException e) {
                logger.error("Exception: " + e);
                return null;
            } catch (NoSuchProviderException e) {
                logger.error("Exception: " + e);
                return null;
            }
        }
        byte[] testData = new byte[nBytes];
        random.nextBytes(testData);
        return testData;
    }

    public static synchronized String getAdminUserName() {
        return ADMIN_NAME;
    }

    /**
     * Modify the value of the specified parameter in the metadata.  If value is null, remove the parameter.
     * @param metadata The metadata to be changed.
     * @param param Name of the parameter to be changed.
     * @param value New value of the parameter.
     * @return The changed metadata.
     */
    public static String modifyMetadataValue(final String metadata, final String param, final String value) {
        int paramIndex = metadata.indexOf(param);
        int valueIndex = metadata.indexOf(":", paramIndex + 1) + 1;
        int nextIndex = metadata.indexOf(",", valueIndex);
        String changed;
        if (value == null) {
            if (nextIndex == -1) {
                changed = metadata.substring(0, paramIndex);
            } else {
                changed = metadata.substring(0, paramIndex) + metadata.substring(nextIndex);
            }
        } else {
            if (nextIndex == -1) {
                changed = metadata.substring(0, valueIndex) + value;
            } else {
                changed = metadata.substring(0, valueIndex) + value + metadata.substring(nextIndex);
            }
        }
        return changed;
    }

    public static KmcCryptoManager getTestingCryptoManager() throws KmcCryptoManagerException{ 
        KmcCryptoManager cryptoManager = new KmcCryptoManager(null);
        //cryptoManager.setUseCryptoService("true");
        return cryptoManager;

    }

}
