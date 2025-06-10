package gov.nasa.jpl.ammos.asec.kmc.cli.crud;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssnValidator;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ServiceType;
import gov.nasa.jpl.ammos.asec.kmc.api.sadb.IDbSession;
import gov.nasa.jpl.ammos.asec.kmc.api.sadb.IKmcDao;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;

/**
 * Base class for creating and updating Security Associations
 *
 */
@CommandLine.Command
public abstract class BaseCreateUpdate extends BaseCliApp {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    protected Integer     spi;
    protected Short       scid;
    protected Byte        mapId;
    protected Byte        tfvn;
    protected Byte        vcid;
    protected boolean     updateEnc;
    protected boolean     updateAuth;
    protected boolean     updateAbm;
    protected ServiceType st;
    protected byte[]      ecsBytes;
    protected byte[]      arsnBytes;
    protected byte[]      ivBytes;
    protected byte[]      abmBytes;
    protected byte[]      acsBytes;
    protected Short       arsnw;
    protected String      ekid;
    protected String      akid;
    protected Short       arsnlen;
    protected Short       ivLen;
    protected Integer     abmLen;
    protected Short       shivfLen;
    protected Short       shplfLen;
    protected Short       shsnfLen;
    protected Short       stmacfLen;
    protected File        file;
    protected Mode        mode = Mode.UNKNOWN;

    protected final String user = System.getProperty("user.name");

    protected enum Mode {
        SINGLE, BULK, UNKNOWN
    }

    static class BulkArgs {
        @CommandLine.Option(names = "--file", required = true)
        String file;
    }

    @Override
    public Integer call() throws Exception {
        int exit = 0;
        try {
            checkAndSetArgs();
            switch (mode) {
                case SINGLE:
                    doSingle();
                    break;
                case BULK:
                    doBulk();
                    break;
                default:
                    exit = printHelp();
            }
        } catch (Exception e) {
            error(e.getMessage(), e);
            exit = 1;

        }
        return exit;
    }

    abstract void doSingle() throws KmcException;

    abstract void doBulk() throws IOException, KmcException;

    protected void checkSt(String stStr) throws KmcException {
        if (stStr != null) {
            try {
                Short parsed = Short.parseShort(stStr);
                st = SecAssnValidator.checkSt(parsed);
            } catch (NumberFormatException e) {
                st = SecAssnValidator.checkSt(stStr);
            }
        }
    }

    protected void updateSa(final SecAssn sa, IKmcDao dao, IDbSession session) throws KmcException {
        SecAssn mutableSa   = sa;
        boolean needsUpdate = false;
        if (updateEnc) {
            console(String.format("%s updating encryption key on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            dao.rekeySaEnc(session, mutableSa.getId(), ekid, ecsBytes, (short) 1);
            session.flush();
            mutableSa = dao.getSa(session, mutableSa.getId());
        }
        if (updateAuth) {
            console(String.format("%s updating authentication key on SA %s/%s", user,
                    mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            dao.rekeySaAuth(session, mutableSa.getId(), akid, acsBytes, (short) 1);
            session.flush();
            mutableSa = dao.getSa(session, mutableSa.getId());
        }
        if (arsnBytes != null) {
            console(String.format("%s updating ARSN on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setArsn(arsnBytes);
            mutableSa.setArsnLen(arsnlen);
            needsUpdate |= true;
        }
        if (arsnw != null) {
            console(String.format("%s updating ARSNW on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setArsnw(arsnw);
            needsUpdate |= true;
        }
        if (ivBytes != null) {
            console(String.format("%s updating IV on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setIv(ivBytes);
            mutableSa.setIvLen(ivLen);
            needsUpdate |= true;
        }
        if (updateAbm) {
            console(String.format("%s updating ABM on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setAbm(abmBytes);
            mutableSa.setAbmLen(abmLen);
            needsUpdate |= true;
        }
        if (st != null) {
            console(String.format("%s updating ST on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setEst(st.getEncryptionType());
            mutableSa.setAst(st.getAuthenticationType());

            needsUpdate |= true;
        }
        if (shivfLen != null) {
            console(String.format("%s updating SHIVF length on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setShivfLen(shivfLen);
            needsUpdate |= true;
        }
        if (shplfLen != null) {
            console(String.format("%s updating SHPLF length on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setShplfLen(shplfLen);
            needsUpdate |= true;
        }
        if (shsnfLen != null) {
            console(String.format("%s updating SHSNF length on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setShsnfLen(shsnfLen);
            needsUpdate |= true;
        }
        if (stmacfLen != null) {
            console(String.format("%s updating STMACF length on SA %s/%s", user, mutableSa.getId().getSpi(),
                    mutableSa.getId().getScid()));
            mutableSa.setStmacfLen(stmacfLen);
            needsUpdate |= true;
        }

        if (needsUpdate) {
            dao.updateSa(session, mutableSa);
            session.flush();
        } else {
            warn(String.format("SA %d/%d nothing to update", sa.getSpi(), sa.getScid()));
        }
    }

    protected void checkIvParams(String iv, Short ivLen,String stStr,String ecsBytes) throws KmcException {
        checkSt(stStr);
        Short encryptionType=null;
        if (st != null){
            encryptionType= st.getEncryptionType();
        }
        this.ivBytes = SecAssnValidator.verifyIv(iv, ivLen, encryptionType, ecsBytes);
        this.ivLen = ivLen;
    }

    protected void checkAuthParams(String akid, String acs) throws KmcException {
        this.acsBytes = SecAssnValidator.verifyAuth(akid, acs);
        this.akid = akid;
        if (acsBytes != null) {
            updateAuth = true;
        }
    }

    protected void checkEncParams(String ekid, String ecs) throws KmcException {
        this.ecsBytes = SecAssnValidator.verifyEnc(ekid, ecs);
        this.ekid = ekid;
        if (ecsBytes != null) {
            updateEnc = true;
        }
    }

    protected void checkArsnParams(String arsn, Short arsnlen) throws KmcException {
        this.arsnBytes = SecAssnValidator.verifyArsn(arsn, arsnlen);
        this.arsnlen = arsnlen;
    }

    protected void checkArsnWParams(Short arsnw) throws KmcException {
        SecAssnValidator.verifyArsnw(arsnw);
        this.arsnw = arsnw;
    }

    protected void checkAbmParams(String abm, Integer abmLen) throws KmcException {
        this.abmBytes = SecAssnValidator.checkAbm(abm, abmLen);
        this.abmLen = abmLen;
        this.updateAbm = abmBytes != null;
    }

    protected void checkShivfLen(Short shifvLen) {
        this.shivfLen = shifvLen;
    }

    protected void checkShplfLen(Short shplfLen) {
        this.shplfLen = shplfLen;
    }

    protected void checkShsnfLen(Short shsnfLen) {
        this.shsnfLen = shsnfLen;
    }

    protected void checkStmacfLen(Short stmacfLen) {
        this.stmacfLen = stmacfLen;
    }

    abstract void checkAndSetArgs() throws KmcException;

    protected int printHelp() {
        CommandLine.usage(this, System.err);
        return 1;
    }

    protected void checkAndSetSpi(Integer spi) {
        this.spi = spi;
    }

    protected void checkAndSetScid(Short scid) {
        this.scid = scid;
    }

    protected void checkAndSetMapId(Byte mapId) {
        this.mapId = mapId;
    }

    protected void checkAndSetTfvn(Byte tfvn) {
        this.tfvn = tfvn;
    }

    protected void checkAndSetVcid(Byte vcid) {
        this.vcid = vcid;
    }

}
