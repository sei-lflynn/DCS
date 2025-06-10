package gov.nasa.jpl.ammos.asec.kmc.api.sa;

import java.util.EnumMap;

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
public class SecAssn implements ISecAssn {
    private static final ObjectMapper mapper = new ObjectMapper();


    /**
     * Constructor
     */
    public SecAssn() {
        this(new SpiScid());
    }

    public SecAssn(SpiScid id) {
        this.id = id;
    }

    @EmbeddedId
    private SpiScid id;
    // transfer frame version number
    private Byte    tfvn;
    // virtual channel id
    private Byte    vcid;
    // multiplexer access point id
    private Byte    mapid;
    // encryption key id (ref)
    @Column(length = 100)
    private String  ekid;
    // authentication key id (ref)
    @Column(length = 100)
    private String  akid;
    // sa state
    @Column(name = "sa_state", nullable = false)
    private Short   saState   = 1;
    // ??
    private Short   lpid;
    // encryption service type
    private Short   est       = 0;
    // authentication service type
    private Short   ast       = 0;
    // security header iv field len
    @Column(name = "shivf_len")
    private Short   shivfLen  = 12;
    // security header sn field len
    @Column(name = "shsnf_len")
    private Short   shsnfLen  = 0;
    // security header pl field len
    @Column(name = "shplf_len")
    private Short   shplfLen  = 0;
    // security trailer mac field len
    @Column(name = "stmacf_len")
    private Short   stmacfLen = 0;
    // encryption cipher suite len
    @Column(name = "ecs_len")
    private Short   ecsLen    = 1;
    // encryption cipher suite (algorithm / mode id)
    @JsonSerialize(using = ByteArraySerializer.class)
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private byte[]  ecs       = ECSTYPE.AES_GCM.getValue();
    // initialization vector len
    @Column(name = "iv_len")
    private Short   ivLen     = 12;
    // initialization vector
    @JsonSerialize(using = ByteArraySerializer.class)
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private byte[]  iv        = null; 
    //before AKMC-239, IV would default to: new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    
    // authentication cipher suite len
    @Column(name = "acs_len")
    private Short   acsLen    = 0;
    // authentication cipher suite (algorithm / mode id)
    @JsonSerialize(using = ByteArraySerializer.class)
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private byte[]  acs       = new byte[]{0x00};
    // authentication bit mask len
    @Column(name = "abm_len")
    private Integer abmLen    = 19;
    // authentication bit mask (primary header through security header)
    @JsonSerialize(using = ByteArraySerializer.class)
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    @Column(name = "abm")
    private byte[]  abm       = new byte[]{0x00,
            0x00,
            (byte) 0xFC,
            0x00,
            0x00,
            (byte) 0xFF,
            (byte) 0xFF,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00};
    // anti replay counter len
    @Column(name = "arsn_len")
    private Short   arsnLen   = 20;
    // anti replay counter
    @JsonSerialize(using = ByteArraySerializer.class)
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private byte[]  arsn      = new byte[]{0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00,
            0x00};
    // anti replay counter window
    private Short   arsnw     = 0;

    @Override
    public String toString() {
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setId(SpiScid id) {
        this.id = id;
    }

    public SpiScid getId() {
        return id;
    }

    public void setAkid(String akid) {
        this.akid = akid;
    }

    public String getAkid() {
        return akid;
    }

    public Integer getSpi() {
        return id.getSpi();
    }

    public void setSpi(Integer spi) {
        this.id.setSpi(spi);
    }

    public String getEkid() {
        return ekid;
    }

    public void setEkid(String ekid) {
        this.ekid = ekid;
    }

    public Short getSaState() {
        return saState;
    }

    public void setSaState(Short saState) {
        this.saState = saState;
    }

    public Byte getTfvn() {
        return tfvn;
    }

    public void setTfvn(Byte tfvn) {
        this.tfvn = tfvn;
    }

    public Short getScid() {
        return id.getScid();
    }

    public void setScid(Short scid) {
        this.id.setScid(scid);
    }

    public Byte getVcid() {
        return this.vcid;
    }

    public void setVcid(Byte vcid) {
        this.vcid = vcid;
    }

    public Byte getMapid() {
        return this.mapid;
    }

    public void setMapid(Byte mapid) {
        this.mapid = mapid;
    }

    public Short getLpid() {
        return lpid;
    }

    public void setLpid(Short lpid) {
        this.lpid = lpid;
    }

    public Short getEst() {
        return est;
    }

    public void setEst(Short est) {
        this.est = est;
    }

    public Short getAst() {
        return ast;
    }

    public void setAst(Short ast) {
        this.ast = ast;
    }

    public Short getShivfLen() {
        return shivfLen;
    }

    public void setShivfLen(Short shivfLen) {
        this.shivfLen = shivfLen;
    }

    public Short getShsnfLen() {
        return shsnfLen;
    }

    public void setShsnfLen(Short shsnfLen) {
        this.shsnfLen = shsnfLen;
    }

    public Short getShplfLen() {
        return shplfLen;
    }

    public void setShplfLen(Short shplfLen) {
        this.shplfLen = shplfLen;
    }

    public Short getStmacfLen() {
        return stmacfLen;
    }

    public void setStmacfLen(Short stmacfLen) {
        this.stmacfLen = stmacfLen;
    }

    public Short getEcsLen() {
        return ecsLen;
    }

    public void setEcsLen(Short ecsLen) {
        this.ecsLen = ecsLen;
    }

    public byte[] getEcs() {
        return ecs;
    }

    public void setEcs(byte[] ecs) {
        this.ecs = ecs;
    }

    public Short getIvLen() {
        return ivLen;
    }

    public void setIvLen(Short ivLen) {
        this.ivLen = ivLen;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public Short getAcsLen() {
        return acsLen;
    }

    public void setAcsLen(Short acsLen) {
        this.acsLen = acsLen;
    }

    public byte[] getAcs() {
        return acs;
    }

    public void setAcs(byte[] acs) {
        this.acs = acs;
    }

    public Integer getAbmLen() {
        return abmLen;
    }

    public void setAbmLen(int abmLen) {
        this.abmLen = abmLen;
    }

    public byte[] getAbm() {
        return abm;
    }

    public void setAbm(byte[] abm) {
        this.abm = abm;
    }

    public Short getArsnLen() {
        return arsnLen;
    }

    public void setArsnLen(Short arcLen) {
        this.arsnLen = arcLen;
    }

    public byte[] getArsn() {
        return arsn;
    }

    public void setArsn(byte[] arc) {
        this.arsn = arc;
    }

    public Short getArsnw() {
        return arsnw;
    }

    public void setArsnw(Short arcw) {
        this.arsnw = arcw;
    }

    public ServiceType getServiceType() {
        return ServiceType.getServiceType(getEst(), getAst());
    }

    public void setServiceType(ServiceType serviceType) {
        this.setEst(serviceType.getEncryptionType());
        this.setAst(serviceType.getAuthenticationType());
    }
}
