package gov.nasa.jpl.ammos.asec.kmc.api.ex;

/**
 * KMC Exception
 *
 */
public class KmcException extends Exception {

    public KmcException() {
        super();
    }

    public KmcException(String format) {
        super(format);
    }

    public KmcException(Exception e) {
        super(e);
    }

    public KmcException(String message, Throwable cause) {
        super(message, cause);
    }
}
