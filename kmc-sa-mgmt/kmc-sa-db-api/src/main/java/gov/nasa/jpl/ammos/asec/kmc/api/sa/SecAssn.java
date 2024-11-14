package gov.nasa.jpl.ammos.asec.kmc.api.sa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import gov.nasa.jpl.ammos.asec.kmc.api.json.ByteArrayDeserializer;
import gov.nasa.jpl.ammos.asec.kmc.api.json.ByteArraySerializer;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Security Assocation
 * <p>
 * // todo: move initial values to config
 *
 */
@Entity(name = "SecAssn")
@Table(name = "security_associations")
public class SecAssn extends ASecAssn {


    /**
     * Constructor
     */
    public SecAssn() {
        this(new SpiScid());
    }

    public SecAssn(SpiScid id) {
        super(id);
    }
}
