package gov.nasa.jpl.ammos.asec.kmc.api.ex;

/**
 * Stop SA exception
 */
public class KmcStopException extends KmcException {
    /**
     * Constructor
     *
     * @param format
     */
    public KmcStopException(String format) {
        super(format);
    }
}
