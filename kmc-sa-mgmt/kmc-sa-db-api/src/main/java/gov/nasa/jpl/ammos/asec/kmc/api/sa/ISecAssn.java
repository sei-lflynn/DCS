package gov.nasa.jpl.ammos.asec.kmc.api.sa;

public interface ISecAssn {

    void setId(SpiScid id);

    SpiScid getId();

    void setAkid(String akid);

    String getAkid();

    Integer getSpi();

    void setSpi(Integer spi);

    String getEkid();

    void setEkid(String ekid);

    Short getSaState();

    void setSaState(Short saState);

    Byte getTfvn();

    void setTfvn(Byte tfvn);

    Short getScid();

    void setScid(Short scid);

    Byte getVcid();

    void setVcid(Byte vcid);

    Byte getMapid();

    void setMapid(Byte mapid);

    Short getLpid();

    void setLpid(Short lpid);

    Short getEst();

    void setEst(Short est);

    Short getAst();

    void setAst(Short ast);

    Short getShivfLen();

    void setShivfLen(Short shivfLen);

    Short getShsnfLen();

    void setShsnfLen(Short shsnfLen);

    Short getShplfLen();

    void setShplfLen(Short shplfLen);

    Short getStmacfLen();

    void setStmacfLen(Short stmacfLen);

    Short getEcsLen();

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
