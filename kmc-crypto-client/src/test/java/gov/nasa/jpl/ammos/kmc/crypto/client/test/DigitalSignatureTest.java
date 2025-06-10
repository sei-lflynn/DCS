package gov.nasa.jpl.ammos.kmc.crypto.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.util.Random;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import gov.nasa.jpl.ammos.kmc.crypto.IcvCreator;
import gov.nasa.jpl.ammos.kmc.crypto.IcvVerifier;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException.KmcCryptoErrorCode;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManager;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManagerException;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManagerException.KmcCryptoManagerErrorCode;

/**
 * Unit tests for Digital Signature.
 *
 *
 */
public class DigitalSignatureTest {
    private static final String KEYNAME_HEAD = "kmc/test/";
    private static final String[] KEYREFS = new String[] {
        KEYNAME_HEAD + "RSA2048", KEYNAME_HEAD + "RSA3072", KEYNAME_HEAD + "RSA4096"
    };

    private static final String[] ALGORITHMS = new String[] {
        "SHA1withRSA", "SHA256withRSA", "SHA384withRSA", "SHA512withRSA"
    };
    private static final String NOT_ALLOWED_ALGORITHM = "SHA224withRSA";

    private static KmcCryptoManager cryptoManager;

    @BeforeClass
    public static void setUp() throws KmcCryptoManagerException {
        cryptoManager = new KmcCryptoManager(null);
        //cryptoManager.setUseCryptoService("true");
    }


    @Test
    public final void testDigitalSignatureStreamAll()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        for (int i = 0; i < ALGORITHMS.length; i++) {
            for (int j = 0; j < KEYREFS.length; j++) {
                testDigitalSignatureOfStream(ALGORITHMS[i], KEYREFS[j]);
            }
        }
    }

    @Test
    public final void testDigitalSignatureBytesAll()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        for (int i = 0; i < ALGORITHMS.length; i++) {
            for (int j = 0; j < KEYREFS.length; j++) {
                testDigitalSignatureOfBytes(ALGORITHMS[i], KEYREFS[j]);
            }
        }
    }

    @Test
    public final void testDigitalSignatureStringAll()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        for (int i = 0; i < ALGORITHMS.length; i++) {
            for (int j = 0; j < KEYREFS.length; j++) {
                testDigitalSignatureOfString(ALGORITHMS[i], KEYREFS[j]);
            }
        }
    }

    @Test
    public final void useDefaultAlgorithm()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        // create IcvCreator with default algorithm
        IcvCreator creator = cryptoManager.createIcvCreator(KEYREFS[0]);

        // generate signature
        String testString = "This is the string for testing digital signature";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        String metadata = creator.createIntegrityCheckValue(bis);

        // verify the test string with the signature
        bis.reset();
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        assertTrue(verifier.verifyIntegrityCheckValue(bis, metadata));
    }

    @Test
    public final void setDefaultAlgorithm()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        // set default algorithm
        String defaultAlgorithm = "SHA512withRSA";
        String[] args = new String[] {"-default_digital_signature_algorithm=" + defaultAlgorithm};
        KmcCryptoManager myManager = new KmcCryptoManager(args);
        //myManager.setUseCryptoService("true");

        // create IcvCreator with default algorithm
        IcvCreator creator = myManager.createIcvCreator(KEYREFS[0]);

        String testString = "This is the string for testing digital signature";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        String metadata = creator.createIntegrityCheckValue(bis);
        assertTrue(metadata.contains("cryptoAlgorithm:" + defaultAlgorithm));

        // verify the test string with the signature
        bis.reset();
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        assertTrue(verifier.verifyIntegrityCheckValue(bis, metadata));
    }

    private void testDigitalSignatureOfStream(final String algorithm, final String keyRef)
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        cryptoManager.setDigitalSignatureAlgorithm(algorithm);

        byte[] data = new byte[1000000];
        Random random = new Random();
        random.nextBytes(data);
        InputStream is = new ByteArrayInputStream(data);

        // create signature for the test data
        IcvCreator creator = cryptoManager.createIcvCreator(keyRef);
        String metadata = creator.createIntegrityCheckValue(is);

        // verify the test stream with the signature
        is.reset();
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        assertTrue(verifier.verifyIntegrityCheckValue(is, metadata));
    }

    private void testDigitalSignatureOfBytes(final String algorithm, final String keyRef)
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        cryptoManager.setDigitalSignatureAlgorithm(algorithm);

        byte[] data = new byte[100];
        Random random = new Random();
        random.nextBytes(data);
        InputStream is = new ByteArrayInputStream(data);

        // create signature for the test bytes
        IcvCreator creator = cryptoManager.createIcvCreator(keyRef);
        String metadata = creator.createIntegrityCheckValue(is);

        // verify the test bytes with the signature
        is.reset();
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        assertTrue(verifier.verifyIntegrityCheckValue(is, metadata));
    }

    private void testDigitalSignatureOfString(final String algorithm, final String keyRef)
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        cryptoManager.setDigitalSignatureAlgorithm(algorithm);

        String testString = "This is the string for testing digital signature";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));

        // create signature for the test string
        IcvCreator creator = cryptoManager.createIcvCreator(keyRef);
        String metadata = creator.createIntegrityCheckValue(bis);

        // verify the test string with the signature
        bis.reset();
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        assertTrue(verifier.verifyIntegrityCheckValue(bis, metadata));
    }

    @Test
    public final void testRepeatedUse()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        // repeated use of the same creator and verifier
        IcvCreator creator = cryptoManager.createIcvCreator(KEYREFS[0]);
        IcvVerifier verifier = cryptoManager.createIcvVerifier();

        String testString1 = "String 1 for testing repeated use of the IcvCreator and IcvVerifier.";
        String testString2 = "String 2 for testing repeated use of the IcvCreator and IcvVerifier.";
        InputStream bis1 = new ByteArrayInputStream(testString1.getBytes("UTF-8"));
        InputStream bis2 = new ByteArrayInputStream(testString2.getBytes("UTF-8"));

        String metadata1 = creator.createIntegrityCheckValue(bis1);
        String metadata2 = creator.createIntegrityCheckValue(bis2);
        bis1.reset();
        bis2.reset();
        assertTrue(verifier.verifyIntegrityCheckValue(bis1, metadata1));
        assertTrue(verifier.verifyIntegrityCheckValue(bis2, metadata2));

        // use a different metadata
        bis2.reset();
        assertFalse(verifier.verifyIntegrityCheckValue(bis2, metadata1));

        // same signature for same input string.
        bis1.reset();
        String metadata3 = creator.createIntegrityCheckValue(bis1);
        assertEquals(metadata1, metadata3);
    }

    // Anomalies

    @Test
    public final void testNullAlgorithm() throws KmcCryptoManagerException  {
        try {
            cryptoManager.setDigitalSignatureAlgorithm(null);
            cryptoManager.createIcvCreator(KEYREFS[0]);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoManagerException e) {
            assertEquals(KmcCryptoManagerErrorCode.CONFIG_PARAMETER_VALUE_INVALID, e.getErrorCode());
        }
    }

    @Test
    public final void testNotAllowedAlgorithm() throws KmcCryptoManagerException  {
        try {
            cryptoManager.setDigitalSignatureAlgorithm(NOT_ALLOWED_ALGORITHM);
            cryptoManager.createIcvCreator(KEYREFS[0]);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoManagerException e) {
            assertEquals(KmcCryptoManagerErrorCode.CONFIG_PARAMETER_VALUE_INVALID, e.getErrorCode());
        }
    }

    @Test
    public final void testNullKey() throws KmcCryptoManagerException  {
        try {
            cryptoManager.createIcvCreator(null);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoManagerException e) {
            assertEquals(KmcCryptoManagerErrorCode.NULL_VALUE, e.getErrorCode());
        }
    }

    @Test
    public final void testNonExistKey() throws KmcCryptoManagerException, KmcCryptoException, UnsupportedEncodingException  {
        try {
            cryptoManager.createIcvCreator("kmc/test/nonExistKey");
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoManagerException e) {
            assertEquals(KmcCryptoManagerErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
        }
    }
/*
    @Test
    public final void testWrongKeyAlgorithm() throws KmcCryptoManagerException, KmcCryptoException  {
        try {
            cryptoManager.createIcvCreator(WRONG_ALGORITHM_KEYREF);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoManagerException e) {
            assertEquals(KmcCryptoManagerErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
        }
        try {
            cryptoManager.createIcvCreator(
                    keystorePath + "symmetric-keys.jck", KEYSTORE_PASS,
                    KeystoreKeyServiceClient.KEYSTORE_TYPE_JCEKS, WRONG_ALGORITHM_KEYREF, KEYSTORE_KEYPASS);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoManagerException e) {
            assertEquals(KmcCryptoManagerErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
        }
    }
*/
    @Test
    public final void testNullInputStream() throws KmcCryptoManagerException, KmcCryptoException  {
        IcvCreator creator = cryptoManager.createIcvCreator(KEYREFS[0]);
        try {
            creator.createIntegrityCheckValue((InputStream) null);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.INVALID_INPUT_VALUE, e.getErrorCode());
        }
    }

    @Test
    public final void testDifferentData()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        IcvCreator creator = cryptoManager.createIcvCreator(KEYREFS[0]);

        // create signature of the test string
        String testString = "This is the test string for verifying a different input data.";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        String metadata = creator.createIntegrityCheckValue(bis);
        // verify the test string with the signature
        bis.reset();
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        assertTrue(verifier.verifyIntegrityCheckValue(bis, metadata));

        // verify a different string
        String badData = testString.replace('i', 'j');
        InputStream bis2 = new ByteArrayInputStream(badData.getBytes("UTF-8"));
        assertFalse(verifier.verifyIntegrityCheckValue(bis2, metadata));
    }

    @Test
    public final void testCorruptedSignature()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        IcvCreator creator = cryptoManager.createIcvCreator(KEYREFS[0]);

        // create signature of the test string
        String testString = "This is the test string for testing corrupted metadata.";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        String metadata = creator.createIntegrityCheckValue(bis);
        // verify the test string with the signature
        bis.reset();
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        assertTrue(verifier.verifyIntegrityCheckValue(bis, metadata));

        // corrupt the signature by changing its first byte to 'x'
        int i = metadata.indexOf("integrityCheckValue");
        i = metadata.indexOf(":", i + 1) + 1;
        String badMetadata = metadata.substring(0, i);
        char c = metadata.charAt(i);
        if (c == '0') {
            badMetadata = badMetadata + '1' + metadata.substring(i + 1);
        } else {
            badMetadata = badMetadata + '0' + metadata.substring(i + 1);
        }
        bis.reset();
        assertFalse(verifier.verifyIntegrityCheckValue(bis, badMetadata));
    }

    @Test
    public final void testInvalidMetadata()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        IcvCreator creator = cryptoManager.createIcvCreator(KEYREFS[0]);

        // create signature of the test string
        String testString = "This is the test string for testing invalid metadata.";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        String metadata = creator.createIntegrityCheckValue(bis);
        // verify the test string with the signature
        bis.reset();
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        assertTrue(verifier.verifyIntegrityCheckValue(bis, metadata));

        // make the metadata invalid by changing ':' into '='
        String invalidMetadata = metadata.replace(':', '=');

        bis.reset();
        try {
            verifier.verifyIntegrityCheckValue(bis, invalidMetadata);
            fail("Excepted CryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_METADATA_ERROR, e.getErrorCode());
        }
    }

    @Test
    public final void testBadMetadata()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        InputStream is = new ByteArrayInputStream(new byte[]{0});
        IcvVerifier verifier = cryptoManager.createIcvVerifier();

        String disallowedAlg = "metadataType:IntegrityCheckMetadata,"
                + "keyRef:kmc/test/RSA2048,cryptoAlgorithm:SHA224withRSA,"
                + "integrityCheckValue:0123456789";
        try {
            verifier.verifyIntegrityCheckValue(is, disallowedAlg);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_ALGORITHM_ERROR, e.getErrorCode());
        }
    }

    @Test
    public final void testVeriferInvalidInput()
            throws KmcCryptoManagerException, KmcCryptoException, IOException  {
        IcvVerifier verifier = cryptoManager.createIcvVerifier();
        InputStream is = new FileInputStream("/dev/null");
        String metadata = "";
        try {
            verifier.verifyIntegrityCheckValue((InputStream) null, metadata);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.INVALID_INPUT_VALUE, e.getErrorCode());
        }
        try {
            verifier.verifyIntegrityCheckValue(is, null);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.INVALID_INPUT_VALUE, e.getErrorCode());
        }
    }

}
