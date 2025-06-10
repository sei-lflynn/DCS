package gov.nasa.jpl.ammos.asec.kmc.api.sa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class SpiScid implements Serializable {
    private static final ObjectMapper mapper = new ObjectMapper();
    private              Integer      spi;
    private              Short        scid;

    public SpiScid() {

    }

    public SpiScid(Integer spi, Short scid) {
        this.spi = spi;
        this.scid = scid;
    }

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getSpi() {
        return spi;
    }

    public void setSpi(Integer spi) {
        this.spi = spi;
    }

    public Short getScid() {
        return scid;
    }

    public void setScid(Short scid) {
        this.scid = scid;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + spi;
        result = 31 * result + scid;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof SpiScid)) {
            return false;
        }

        SpiScid id = (SpiScid) o;
        return this.spi.equals(id.spi) && this.scid.equals(id.scid);
    }
}
