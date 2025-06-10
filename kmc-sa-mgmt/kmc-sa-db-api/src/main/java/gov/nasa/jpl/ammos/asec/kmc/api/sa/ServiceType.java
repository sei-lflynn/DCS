package gov.nasa.jpl.ammos.asec.kmc.api.sa;

/**
 * Service type enum
 * <p>
 * For any combination of (Encryption, Authentication) service types,
 * PLAINTEXT = (0, 0)
 * ENCRYPTION = (1, 0)
 * AUTHENTICATION = (0, 1)
 * ENCRYPTION_AUTHENTICATION = (1, 1)
 *
 */
public enum ServiceType {
    PLAINTEXT((short) 0, (short) 0),
    ENCRYPTION((short) 1, (short) 0),
    AUTHENTICATION((short) 0, (short) 1),
    AUTHENTICATED_ENCRYPTION((short) 1, (short) 1),
    UNKNOWN((short) -1, (short) -1);

    private final Short enc;
    private final Short auth;

    ServiceType(Short enc, Short auth) {
        this.enc = enc;
        this.auth = auth;
    }

    public static ServiceType fromShort(Short serviceType) {
        if (serviceType == null) {
            return UNKNOWN;
        }
        switch (serviceType) {
            case 0:
                return PLAINTEXT;
            case 1:
                return ENCRYPTION;
            case 2:
                return AUTHENTICATION;
            case 3:
                return AUTHENTICATED_ENCRYPTION;
            default:
                return UNKNOWN;
        }
    }

    public static ServiceType getServiceType(Short enc, Short auth) {
        if (enc == 0 && auth == 0) {
            return PLAINTEXT;
        } else if (enc == 1 && auth == 0) {
            return ENCRYPTION;
        } else if (enc == 0 && auth == 1) {
            return AUTHENTICATION;
        } else {
            return AUTHENTICATED_ENCRYPTION;
        }
    }

    public Short getEncryptionType() {
        if (this == UNKNOWN) {
            return null;
        }
        return this.enc;
    }

    public Short getAuthenticationType() {
        if (this == UNKNOWN) {
            return null;
        }
        return this.auth;
    }
}
