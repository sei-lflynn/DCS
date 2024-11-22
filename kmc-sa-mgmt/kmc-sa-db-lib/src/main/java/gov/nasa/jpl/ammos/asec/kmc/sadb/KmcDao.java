package gov.nasa.jpl.ammos.asec.kmc.sadb;

import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcStartException;
import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcStopException;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.FrameType;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ISecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssnAos;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssnTm;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssnValidator;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SpiScid;
import gov.nasa.jpl.ammos.asec.kmc.api.sadb.IDbSession;
import gov.nasa.jpl.ammos.asec.kmc.api.sadb.IKmcDao;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KmcDao implements IKmcDao {

    private static final Logger LOG                           = LoggerFactory.getLogger(IKmcDao.class);
    public static final  short  SA_UNKEYED                    = 1;
    public static final  short  SA_KEYED                      = 2;
    public static final  short  SA_EXPIRE                     = 1;
    public static final  short  SA_OPERATIONAL                = 3;
    public static final  String ETC_HIBERNATE_CFG_XML         = "etc/hibernate.cfg.xml";
    public static final  String HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";
    public static final  String HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";
    public static final  String HIBERNATE_CONNECTION_URL      = "hibernate.connection.url";

    private final Properties    properties = new Properties();
    private       Configuration config     = new Configuration();

    private SessionFactory factory;

    public KmcDao() {
        config.configure(ETC_HIBERNATE_CFG_XML);
    }

    public KmcDao(String user, char[] password, String url) {
        this(user, new String(password), url);
    }

    public KmcDao(String user, String password, String url) {
        properties.put(HIBERNATE_CONNECTION_USERNAME, user);
        properties.put(HIBERNATE_CONNECTION_PASSWORD, password);
        properties.put(HIBERNATE_CONNECTION_URL, url);
        configure();
    }

    public KmcDao(String user, String password) {
        properties.put(HIBERNATE_CONNECTION_USERNAME, user);
        properties.put(HIBERNATE_CONNECTION_PASSWORD, password);
        configure();
    }

    private void configure() {
        config.configure(ETC_HIBERNATE_CFG_XML);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            config.getProperties().setProperty((String) entry.getKey(), (String) entry.getValue());
        }
    }

    @Override
    public void init() throws KmcException {
        try {
            factory = config.buildSessionFactory();
        } catch (HibernateException e) {
            throw new KmcException(e);
        }
    }

    /**
     * Wrapper class for hibernate sessions
     */

    @Override
    public IDbSession newSession() {
        return new DbSession(factory.openSession());
    }

    @Override
    public void createSa(IDbSession dbSession, Integer spi, Byte tvfn, Short scid, Byte vcid, Byte mapid,
                         FrameType type) throws KmcException {
        isReady();
        if (spi == null) {
            spi = getNextAvailableSpi(scid, type);
        }
        SpiScid  spiScid = new SpiScid(spi, scid);
        ISecAssn sa;
        switch (type) {
            case TM -> sa = new SecAssnTm(spiScid);
            case AOS -> sa = new SecAssnAos(spiScid);
            default -> sa = new SecAssn(spiScid);
        }
        sa.setTfvn(tvfn);
        sa.setVcid(vcid);
        sa.setMapid(mapid);
        sa.setSaState(SA_UNKEYED);
        LOG.info("Creating SA {}/{}", spi, scid);
        dbSession.persist(sa);
        LOG.info("Creating SA {}/{} done", spi, scid);
    }

    @Override
    public ISecAssn createSa(Integer spi, Byte tvfn, Short scid, Byte vcid, Byte mapid, FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            session.beginTransaction();
            createSa(session, spi, tvfn, scid, vcid, mapid, type);
            session.commit();
        } catch (Exception e) {
            LOG.error("Encountered unexpected error while creating SA with the following parameters: " +
                            "spi {}, tvfn {}, scid {}, vcid {}, mapid {}, type {} : {}", spi, tvfn, scid, vcid, mapid
                    , type,
                    e.getMessage());
            throw new KmcException("Unable to create SA due to error: ", e);
        }
        return getSa(new SpiScid(spi, scid), type);
    }

    private Integer getNextAvailableSpi(Short scid, FrameType type) throws KmcException {
        isReady();
        int spi;
        try (Session session = factory.openSession()) {
            Integer maxSpi = session
                    .createQuery("SELECT max(sa.id.spi) FROM " + type.toString() + " sa WHERE sa.id.scid = :scid",
                            Integer.class)
                    .setParameter("scid", scid)
                    .getSingleResult();
            if (maxSpi == null) {
                maxSpi = 0;
            }
            spi = maxSpi + 1;
        } catch (HibernateException e) {
            LOG.error("Encountered Hibernate error while querying for max SPI for SCID {}: {}", scid, e.getMessage());
            throw new KmcException("Unable to get next available SPI due to Hibernate error: ", e);
        } catch (Exception e) {
            LOG.error("Encountered unexpected error while querying for max SPI for SCID {}: {}", scid, e.getMessage());
            throw new KmcException("Unable to get next available SPI due to unexpected error: ", e);
        }
        return spi;
    }

    @Override
    public ISecAssn createSa(ISecAssn sa) throws KmcException {
        try (DbSession session = new DbSession(factory.openSession())) {
            session.beginTransaction();
            createSa(session, sa);
            session.commit();
        } catch (HibernateException e) {
            LOG.error("Encountered Hibernate error while creating SA {}: {}", sa.toString(), e.getMessage());
            throw new KmcException("Unable to create SA due to Hibernate error: ", e);
        } catch (Exception e) {
            LOG.error("Encountered unexpected error while creating SA {}: {}", sa.toString(), e.getMessage());
            throw new KmcException("Unable to create SA due to unexpected error: ", e);
        }
        return getSa(sa.getId(), sa.getType());
    }

    @Override
    public void createSa(IDbSession dbSession, ISecAssn sa) throws KmcException {
        isReady();
        try {
            SecAssnValidator.validate(sa);
        } catch (KmcException e) {
            throw new KmcException(String.format("Cannot create SA: %s", e.getMessage()));
        }
        if (sa.getSpi() == null) {
            sa.setSpi(getNextAvailableSpi(sa.getScid(), sa.getType()));
        } else {
            ISecAssn exists = getSa(dbSession, sa.getId(), sa.getType());
            if (exists != null) {
                throw new KmcException(String.format("SA create failed: an SA with the SPI/SCID combination %d/%d " + "already exists", sa.getSpi(), sa.getScid()));
            }
        }
        // validate more
        if (sa.getSaState() == null) {
            sa.setSaState(SA_UNKEYED);
        }

        LOG.info("Creating SA {}/{}", sa.getSpi(), sa.getScid());
        dbSession.persist(sa);
        LOG.info("Creating SA {}/{} done", sa.getSpi(), sa.getScid());
    }

    @Override
    public void rekeySaEnc(IDbSession session, SpiScid id, String ekid, byte[] ecs, Short ecsLen, FrameType type) throws KmcException {
        isReady();
        ISecAssn sa = getSa(session, id, type);
        if (sa == null) {
            throw new KmcException(String.format("SA %s does not exist, cannot rekey for encryption", id));
        }
        LOG.info("Rekeying SA {}/{} for encryption to EKID {} with ECS {}", id.getSpi(), id.getScid(), ekid, ecs);
        sa.setEkid(ekid);
        sa.setEcs(ecs);
        sa.setEcsLen(ecsLen);
        sa.setSaState(SA_KEYED);
        session.merge(sa);
        LOG.info("Rekeying SA {}/{} for encryption complete", id.getSpi(), id.getScid());
    }

    @Override
    public ISecAssn rekeySaEnc(SpiScid id, String ekid, byte[] ecs, Short ecsLen, FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            session.beginTransaction();
            rekeySaEnc(session, id, ekid, ecs, ecsLen, type);
            session.commit();
        } catch (Exception e) {
            throw new KmcException(e);
        }
        return getSa(id, type);
    }

    @Override
    public void rekeySaAuth(IDbSession session, SpiScid id, String akid, byte[] acs, Short acsLen, FrameType type) throws KmcException {
        isReady();
        ISecAssn sa = getSa(session, id, type);
        if (sa == null) {
            throw new KmcException(String.format("SA %s does not exist, cannot rekey for authentication", id));
        }
        LOG.info("Rekeying SA {}/{} for authentication to AKID {} with ACS {}", sa.getId(), sa.getScid(), akid,
                acs);
        sa.setAkid(akid);
        sa.setAcs(acs);
        sa.setAcsLen(acsLen);
        sa.setSaState(SA_KEYED);
        session.merge(sa);
        LOG.info("Rekeying SA {}/{} for authentication complete", id.getSpi(), id.getScid());
    }

    @Override
    public ISecAssn rekeySaAuth(SpiScid id, String akid, byte[] acs, Short acsLen, FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            session.beginTransaction();
            rekeySaAuth(session, id, akid, acs, acsLen, type);
            session.commit();
        } catch (Exception e) {
            throw new KmcException(e);
        }

        return getSa(id, type);
    }

    @Override
    public void expireSa(IDbSession session, SpiScid id, FrameType type) throws KmcException {
        isReady();
        ISecAssn sa = getSa(id, type);
        if (sa == null) {
            throw new KmcException(String.format("SA %s does not exist, cannot expire", id));
        } else {
            LOG.info("Expiring SA {}/{}", id.getSpi(), id.getScid());
            sa.setSaState(SA_EXPIRE);
            sa.setAkid(null);
            sa.setEkid(null);
            session.merge(sa);
            LOG.info("Expiring SA {}/{} done", id.getSpi(), id.getScid());
        }
    }

    @Override
    public ISecAssn expireSa(SpiScid id, FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            session.beginTransaction();
            expireSa(session, id, type);
            session.commit();
        } catch (Exception e) {
            throw new KmcException(e);
        }
        return getSa(id, type);
    }

    @Override
    public void startSa(IDbSession session, SpiScid id, boolean force, FrameType type) throws KmcException {
        isReady();
        ISecAssn sa = getSa(id, type);
        if (sa == null) {
            throw new KmcStartException(String.format("SA %d/%d does not exist, cannot start", id.getSpi(),
                    id.getScid()));
        } else if (sa.getSaState() == SA_OPERATIONAL) {
            throw new KmcStartException(String.format("SA %d/%d is already operational", id.getSpi(), id.getScid()));
        } else {
            Query<? extends ISecAssn> q = ((DbSession) session).getSession().createQuery("FROM " + type.toString() +
                    " AS sa WHERE sa" +
                    ".id.scid = :scid" +
                    " AND " +
                    "sa.tfvn = :tfvn" +
                    " AND " +
                    "sa.vcid = :vcid" +
                    " AND " +
                    "sa.mapid = :mapid" +
                    " AND " +
                    "sa.id.spi != :subjectSpi", SecAssn.class);
            q.setParameter("scid", sa.getId().getScid());
            q.setParameter("tfvn", sa.getTfvn());
            q.setParameter("vcid", sa.getVcid());
            q.setParameter("mapid", sa.getMapid());
            q.setParameter("subjectSpi", sa.getSpi());
            List<? extends ISecAssn> sas = q.list();
            for (ISecAssn s : sas) {
                if (s.getSaState() == SA_OPERATIONAL) {
                    if (force) {
                        stopSa(s.getId(), type);
                        LOG.info("SA {}/{} is already operational, stop has been forced", s.getSpi(), s.getScid());
                    } else {
                        throw new KmcStartException(String.format("SA %d/%d is already operational for GVCID " +
                                        "(scid " + "%d, tfvn " + "%d, vcid %d, mapid %d)", s.getSpi(), s.getScid(),
                                s.getScid(), s.getTfvn(), s.getVcid(), s.getMapid()));
                    }
                }
            }
            LOG.info("Starting SA {}/{}", id.getSpi(), id.getScid());
            sa.setSaState(SA_OPERATIONAL);
            session.merge(sa);
            LOG.info("Starting SA {}/{} done", id.getSpi(), id.getScid());
        }
    }

    @Override
    public ISecAssn startSa(SpiScid id, boolean force, FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            session.beginTransaction();
            startSa(session, id, force, type);
            session.commit();
        } catch (Exception e) {
            if (e instanceof KmcStartException) {
                throw (KmcStartException) e;
            }
            throw new KmcException(e);
        }
        return getSa(id, type);
    }

    @Override
    public void stopSa(IDbSession session, SpiScid id, FrameType type) throws KmcException {
        isReady();
        ISecAssn sa = getSa(id, type);
        if (sa == null) {
            throw new KmcStopException(String.format("SA %d/%d does not exist, cannot stop", id.getSpi(),
                    id.getScid()));
        } else if (sa.getSaState() != SA_OPERATIONAL) {
            throw new KmcStopException(String.format("SA %d/%d is not operational, cannot stop", id.getSpi(),
                    id.getScid()));
        } else {
            LOG.info("Stopping SA {}/{}", id.getSpi(), id.getScid());
            sa.setSaState(SA_KEYED);
            session.merge(sa);
            LOG.info("Stopping SA {}/{} done", id.getSpi(), id.getScid());
        }
    }

    @Override
    public ISecAssn stopSa(SpiScid id, FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            session.beginTransaction();
            stopSa(session, id, type);
            session.commit();
        } catch (Exception e) {
            if (e instanceof KmcStopException) {
                throw (KmcStopException) e;
            }
            throw new KmcException(e);
        }
        return getSa(id, type);
    }

    @Override
    public void deleteSa(IDbSession session, SpiScid id, FrameType type) throws KmcException {
        isReady();
        ISecAssn sa = getSa(id, type);
        if (sa == null) {
            throw new KmcException(String.format("SA %s does not exist, cannot delete", id));
        } else {
            LOG.info("Deleting SA {}/{}", id.getSpi(), id.getScid());
            session.remove(sa);
            LOG.info("Deleting SA {}/{} done", id.getSpi(), id.getScid());
        }
    }

    @Override
    public void deleteSa(SpiScid id, FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            session.beginTransaction();
            deleteSa(session, id, type);
            session.commit();
        } catch (KmcException e) {
            throw e;
        } catch (Exception e) {
            throw new KmcException(e);
        }
    }

    @Override
    public ISecAssn getSa(IDbSession session, SpiScid id, FrameType type) throws KmcException {
        isReady();
        return ((DbSession) session).getSession().find(type.getClazz(), id);
    }

    @Override
    public ISecAssn getSa(SpiScid id, FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            return getSa(session, id, type);
        } catch (KmcException e) {
            throw e;
        } catch (Exception e) {
            throw new KmcException(e);
        }
    }

    @Override
    public List<? extends ISecAssn> getSas(IDbSession session, FrameType type) throws KmcException {
        isReady();
        return ((DbSession) session).getSession()
                .createQuery("FROM " + type.toString(), type.getClazz())
                .list();
    }

    @Override
    public List<? extends ISecAssn> getSas(FrameType type) throws KmcException {
        try (IDbSession session = newSession()) {
            return getSas(session, type);
        } catch (KmcException e) {
            throw e;
        } catch (Exception e) {
            throw new KmcException(e);
        }
    }

    public List<? extends ISecAssn> getActiveSas(FrameType type) throws KmcException {
        isReady();
        try (IDbSession session = newSession()) {
            Query<? extends ISecAssn> query = ((DbSession) session).getSession()
                    .createQuery("FROM " + type.toString() + " as sa WHERE sa.saState = :state", type.getClazz());
            query.setParameter("state", SA_OPERATIONAL);
            return query.list();
        } catch (Exception e) {
            throw new KmcException(e);
        }
    }

    @Override
    public void updateSa(IDbSession session, ISecAssn sa) throws KmcException {
        isReady();
        LOG.info("Updating SA {}/{}", sa.getId().getSpi(), sa.getId().getScid());
        session.merge(sa);
        LOG.info("Updated SA {}/{}", sa.getId().getSpi(), sa.getId().getScid());
    }

    @Override
    public ISecAssn updateSa(ISecAssn sa) throws KmcException {
        try (IDbSession session = newSession()) {
            session.beginTransaction();
            updateSa(session, sa);
            session.commit();
        } catch (KmcException e) {
            throw e;
        } catch (Exception e) {
            throw new KmcException(e);
        }
        return getSa(sa.getId(), sa.getType());
    }

    @Override
    public void close() throws KmcException {
        if (factory == null) {
            return;
        }
        factory.close();
    }

    private synchronized boolean isReady() throws KmcException {
        if (factory == null || factory.isClosed()) {
            config = new Configuration();
            configure();
            init();
        }
        return true;
    }

    @Override
    public boolean status() {
        try {
            isReady();
        } catch (KmcException e) {
            return false;
        }

        try (Session session = factory.openSession()) {
            session.doWork(connection -> connection.isValid(1000));
        } catch (HibernateException e) {
            LOG.error("Error validating database connection", e);
            return false;
        }

        return true;
    }
}
