package gov.nasa.jpl.ammos.asec.kmc.api.sa;

public interface ISecAssn {

    /**
     * Set SPI/SCID
     *
     * @param id SPI/SCID
     */
    void setId(SpiScid id);

    /**
     * @return SPI/SCID
     */
    SpiScid getId();

    /**
     * Set AKID
     *
     * @param akid AKID
     */
    void setAkid(String akid);

    /**
     * Get AKID
     *
     * @return AKID
     */
    String getAkid();

    /**
     * Get SPI
     *
     * @return SPI
     */
    Integer getSpi();

    /**
     * Set SPI
     *
     * @param spi SPI
     */
    void setSpi(Integer spi);

    /**
     * Get EKID
     *
     * @return EKID
     */
    String getEkid();

    /**
     * Set EKID
     *
     * @param ekid EKID
     */
    void setEkid(String ekid);

    /**
     * Get SA State
     *
     * @return SA State
     */
    Short getSaState();

    /**
     * Set SA State
     *
     * @param saState SA State
     */
    void setSaState(Short saState);

    /**
     * Get TFVN
     *
     * @return TFVN
     */
    Byte getTfvn();

    /**
     * Set TFVN
     *
     * @param tfvn
     */
    void setTfvn(Byte tfvn);

    /**
     * Get SCID
     *
     * @return SCID
     */
    Short getScid();

    /**
     * Set SCID
     *
     * @param scid SCID
     */
    void setScid(Short scid);

    /**
     * Get VCID
     *
     * @return VCID
     */
    Byte getVcid();

    /**
     * Set VCID
     *
     * @param vcid VCID
     */
    void setVcid(Byte vcid);

    /**
     * Get MAP ID
     *
     * @return
     */
    Byte getMapid();

    /**
     * Set MAP ID
     *
     * @param mapid MAP ID
     */
    void setMapid(Byte mapid);

    /**
     * Get LPID
     *
     * @return LPID
     */
    Short getLpid();

    /**
     * Set LPID
     *
     * @param lpid
     */
    void setLpid(Short lpid);

    /**
     * Get EST
     *
     * @return EST
     */
    Short getEst();

    /**
     * Set EST
     *
     * @param est EST
     */
    void setEst(Short est);

    /**
     * Get AST
     *
     * @return AST
     */
    Short getAst();

    /**
     * Set AST
     *
     * @param ast AST
     */
    void setAst(Short ast);

    /**
     * Get SHIVF Length
     *
     * @return SHIVF Length
     */
    Short getShivfLen();

    /**
     * Set SHIVF Length
     *
     * @param shivfLen SHIVF Length
     */
    void setShivfLen(Short shivfLen);

    /**
     * Get SHSNF Length
     *
     * @return SHSNF Length
     */
    Short getShsnfLen();

    /**
     * Set SHSNF Length
     *
     * @param shsnfLen SHSNF Length
     */
    void setShsnfLen(Short shsnfLen);

    /**
     * Get SHPLF Length
     *
     * @return SHPLF Length
     */
    Short getShplfLen();

    /**
     * Set SHPLF Length
     *
     * @param shplfLen SHPLF Length
     */
    void setShplfLen(Short shplfLen);

    /**
     * Get STMACF Length
     *
     * @return STMACF Length
     */
    Short getStmacfLen();

    /**
     * Set STMACF Length
     *
     * @param stmacfLen STMACF Length
     */
    void setStmacfLen(Short stmacfLen);

    /**
     * Get ECS Length
     *
     * @return ECS Length
     */
    Short getEcsLen();

    /**
     * Set ECS Length
     *
     * @param ecsLen ECS Length
     */
    void setEcsLen(Short ecsLen);

    byte[] getEcs();

    void setEcs(byte[] ecs);

    Short getIvLen();

    void setIvLen(Short ivLen);

    byte[] getIv();

    void setIv(byte[] iv);

    Short getAcsLen();

    void setAcsLen(Short acsLen);

    byte[] getAcs();

    void setAcs(byte[] acs);

    Integer getAbmLen();

    void setAbmLen(int abmLen);

    byte[] getAbm();

    void setAbm(byte[] abm);

    Short getArsnLen();

    void setArsnLen(Short arcLen);

    byte[] getArsn();

    void setArsn(byte[] arc);

    Short getArsnw();

    void setArsnw(Short arcw);

    ServiceType getServiceType();

    void setServiceType(ServiceType serviceType);

    FrameType getType();
}
