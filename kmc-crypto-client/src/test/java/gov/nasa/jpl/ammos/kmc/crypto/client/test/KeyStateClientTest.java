package gov.nasa.jpl.ammos.kmc.crypto.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import gov.nasa.jpl.ammos.kmc.crypto.Decrypter;
import gov.nasa.jpl.ammos.kmc.crypto.Encrypter;
import gov.nasa.jpl.ammos.kmc.crypto.IcvCreator;
import gov.nasa.jpl.ammos.kmc.crypto.IcvVerifier;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException.KmcCryptoErrorCode;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManager;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManagerException;

/**
 * Unit tests for enforcing the key is in the right state when used by the crypto functions.
 * For encryption, ICV creation, and signature signing, the key needs to be Active.
 * For decryption, ICV verification, and signature verification, the key needs to be
 * Active, Deactivated, or Compromised.
 *
 * KMIP specifications on State:
 * http://docs.oasis-open.org/kmip/spec/v1.2/csd01/kmip-spec-v1.2-csd01.html#_Toc374096594
 *
 * Ignore these tests as they rely on a KMIP-compliant key management system.
 */
@Ignore
public class KeyStateClientTest {
    private static final String KEYREF_AES_ACTIVE = "kmc/test/AES128";
    private static final String KEYREF_AES_PREACTIVE = "kmc/test/AES_PreActive";
    private static final String KEYREF_AES_DEACTIVATED = "kmc/test/AES_Deactivated";
    private static final String KEYREF_AES_COMPROMISED = "kmc/test/AES_Compromised";
    private static final String KEYREF_HMAC_ACTIVE = "kmc/test/HmacSHA256";
    private static final String KEYREF_HMAC_PREACTIVE = "kmc/test/HMAC_PreActive";
    private static final String KEYREF_HMAC_DEACTIVATED = "kmc/test/HMAC_Deactivated";
    private static final String KEYREF_HMAC_COMPROMISED = "kmc/test/HMAC_Compromised";
    private static final String KEYREF_RSA_ACTIVE = "kmc/test/RSA2048";
    private static final String KEYREF_RSA_PREACTIVE = "kmc/test/RSA_PreActive";
    private static final String KEYREF_RSA_DEACTIVATED = "kmc/test/RSA_Deactivated";
    private static final String KEYREF_RSA_COMPROMISED = "kmc/test/RSA_Compromised";
    private static final int AES_BLOCK_SIZE = 16;  // AES block size in bytes

    private static KmcCryptoManager cryptoManager;

    @BeforeClass
    public static void setUp() throws KmcCryptoManagerException, KmcCryptoException {
        cryptoManager = new KmcCryptoManager(null);
        //cryptoManager.setUseCryptoService("true");
    }


    @Test
    public final void testKeyStateEncrypt()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        Encrypter encrypter;
        String testString = "This is the string for testing key state.";
        int encryptedSize = (testString.length() / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE;
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        ByteArrayOutputStream eos = new ByteArrayOutputStream(encryptedSize);

        // fail to encrypt with PreActive key
        try {
            encrypter = cryptoManager.createEncrypter(KEYREF_AES_PREACTIVE);
            encrypter.encrypt(bis, eos);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_AES_PREACTIVE + " is PreActive"));
        }

        // fail to encrypt with Deactivated key
        bis.reset();
        try {
            encrypter = cryptoManager.createEncrypter(KEYREF_AES_DEACTIVATED);
            encrypter.encrypt(bis, eos);
            fail("Expected KmcCryptoManagerException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_AES_DEACTIVATED + " is Deactivated"));
        }

        // fail to encrypt with Compromised key
        bis.reset();
        try {
            encrypter = cryptoManager.createEncrypter(KEYREF_AES_COMPROMISED);
            encrypter.encrypt(bis, eos);
            fail("Expected KmcCryptoManagerException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_AES_COMPROMISED + " is Compromised"));
        }
    }

    @Test
    public final void testKeyStateDecrypt()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        Encrypter encrypter;
        String testString = "This is the string for testing key state.";
        int encryptedSize = (testString.length() / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE;
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        ByteArrayOutputStream eos = new ByteArrayOutputStream(encryptedSize);
        ByteArrayOutputStream dos = new ByteArrayOutputStream(testString.length());

        // encrypt with an Active key
        encrypter = cryptoManager.createEncrypter(KEYREF_AES_ACTIVE);
        String metadata = encrypter.encrypt(bis, eos);
        byte[] encryptedData = eos.toByteArray();

        Decrypter decrypter = cryptoManager.createDecrypter();
        // decrypt with PreActive key
        // PreActive key cannot be used for decrypt
        String modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_AES_PREACTIVE);
        InputStream eis = new ByteArrayInputStream(encryptedData);
        try {
            decrypter.decrypt(eis, dos, modifiedMetadata);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue("Expected PreActive state but got: " + e.getMessage(),
                e.getMessage().contains("The key state of " + KEYREF_AES_PREACTIVE + " is PreActive"));
        }

        // decrypt with Deactivated key
        // Deactivated key can be used to decrypt but it fails as encrypt was done with a different key.
        modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_AES_DEACTIVATED);
        eis.reset();
        try {
            decrypter.decrypt(eis, dos, modifiedMetadata);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("BadPaddingException"));
        }

        // decrypt with Compromised key
        // Compromised key can be used to decrypt but it fails as encrypt was done with a different key.
        modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_AES_COMPROMISED);
        eis.reset();
        try {
            decrypter.decrypt(eis, dos, modifiedMetadata);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("BadPaddingException"));
        }
    }

    @Test
    public final void testKeyStateIcvCreate()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        IcvCreator creator;
        String testString = "This is the string for testing key state for ICV.";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));

        // fail to create ICV with PreActive key
        try {
            creator = cryptoManager.createIcvCreator(KEYREF_HMAC_PREACTIVE);
            creator.createIntegrityCheckValue(bis);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_HMAC_PREACTIVE + " is PreActive"));
        }

        // fail to create ICV with Deactivated key
        bis.reset();
        try {
            creator = cryptoManager.createIcvCreator(KEYREF_HMAC_DEACTIVATED);
            creator.createIntegrityCheckValue(bis);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_HMAC_DEACTIVATED + " is Deactivated"));
        }

        // fail to create ICV with Compromised key
        bis.reset();
        try {
            creator = cryptoManager.createIcvCreator(KEYREF_HMAC_COMPROMISED);
            creator.createIntegrityCheckValue(bis);
            fail("Expected KmcCryptoException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_HMAC_COMPROMISED + " is Compromised"));
        }
    }

    @Test
    public final void testKeyStateIcvVerify()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        IcvCreator creator;
        String testString = "This is the string for testing key state for ICV.";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        IcvVerifier verifier = cryptoManager.createIcvVerifier();

        // create ICV with Active key
        creator = cryptoManager.createIcvCreator(KEYREF_HMAC_ACTIVE);
        String metadata = creator.createIntegrityCheckValue(bis);

        // verify ICV with PreActive key
        // PreActive key cannot be used for ICV verify
        bis.reset();
        String modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_HMAC_PREACTIVE);
        try {
            verifier.verifyIntegrityCheckValue(bis, modifiedMetadata);
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue("Expected PreActive state but got: " + e.getMessage(),
                e.getMessage().contains("The key state of " + KEYREF_HMAC_PREACTIVE + " is PreActive"));
        }

        // verify ICV with the key creating the ICV
        bis.reset();
        assertTrue(verifier.verifyIntegrityCheckValue(bis, metadata));

        // verify ICV with Deactivated key
        // Deactivated key can be used to verify ICV but it returns false because the ICV was created by a different key.
        modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_HMAC_DEACTIVATED);
        bis.reset();
        assertFalse(verifier.verifyIntegrityCheckValue(bis, modifiedMetadata));

        // verify ICV with Compromised key
        // Compromised key can be used to verify ICV but it returns false because the ICV was created by a different key.
        modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_HMAC_COMPROMISED);
        bis.reset();
        assertFalse(verifier.verifyIntegrityCheckValue(bis, modifiedMetadata));
    }

    @Test
    public final void testKeyStateSignatureCreate()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        IcvCreator signer;
        String testString = "This is the string for testing key state in Digital Signature.";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));

        // fail to create signature with PreActive key
        try {
            signer = cryptoManager.createIcvCreator(KEYREF_RSA_PREACTIVE);
            signer.createIntegrityCheckValue(bis);
            fail("Expected KmcCryptoManagerException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_RSA_PREACTIVE + " is PreActive"));
        }

        // fail to create signature with Deactivated key
        bis.reset();
        try {
            signer = cryptoManager.createIcvCreator(KEYREF_RSA_DEACTIVATED);
            signer.createIntegrityCheckValue(bis);
            fail("Expected KmcCryptoManagerException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_RSA_DEACTIVATED + " is Deactivated"));
        }

        // fail to create signature with Compromised key
        bis.reset();
        try {
            signer = cryptoManager.createIcvCreator(KEYREF_RSA_COMPROMISED);
            signer.createIntegrityCheckValue(bis);
            fail("Expected KmcCryptoManagerException not received.");
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue(e.getMessage().contains("The key state of " + KEYREF_RSA_COMPROMISED + " is Compromised"));
        }
    }

    @Test
    public final void testKeyStateSignatureVerify()
            throws KmcCryptoManagerException, KmcCryptoException, IOException {
        IcvCreator signer;
        String testString = "This is the string for testing key state in Digital Signature.";
        InputStream bis = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        IcvVerifier verifier = cryptoManager.createIcvVerifier();

        // sign with Active key successfully
        signer = cryptoManager.createIcvCreator(KEYREF_RSA_ACTIVE);
        String metadata = signer.createIntegrityCheckValue(bis);

        // verify ICV with PreActive key
        // PreActive key cannot be used for ICV verify
        String modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_RSA_PREACTIVE);
        bis.reset();
        try {
            verifier.verifyIntegrityCheckValue(bis, modifiedMetadata);
        } catch (KmcCryptoException e) {
            assertEquals(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, e.getErrorCode());
            assertTrue("Expected PreActive state but got: " + e.getMessage(),
                e.getMessage().contains("The key state of " + KEYREF_RSA_PREACTIVE + " is PreActive"));
        }

        // verify signature successfully with the same key
        bis.reset();
        assertTrue(verifier.verifyIntegrityCheckValue(bis, metadata));

        // verify signature with Deactivated key
        // Deactivated key can be used to verify signature but it returns false because the signature was created by a different key.
        modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_RSA_DEACTIVATED);
        bis.reset();
        assertFalse(verifier.verifyIntegrityCheckValue(bis, modifiedMetadata));

        // verify signature with Compromised key
        // Compromised key can be used to verify signature but it returns false because the signature was created by a different key.
        modifiedMetadata = CryptoTestUtils.modifyMetadataValue(metadata, "keyRef", KEYREF_RSA_COMPROMISED);
        bis.reset();
        assertFalse(verifier.verifyIntegrityCheckValue(bis, modifiedMetadata));
    }
}
