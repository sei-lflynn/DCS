package gov.nasa.jpl.ammos.asec.kmc.saserver.app.sa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.nasa.jpl.ammos.asec.kmc.api.ex.KmcException;
import gov.nasa.jpl.ammos.asec.kmc.api.json.ByteArrayDeserializer;
import gov.nasa.jpl.ammos.asec.kmc.api.json.ByteArraySerializer;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssn;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SecAssnValidator;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.ServiceType;
import gov.nasa.jpl.ammos.asec.kmc.api.sa.SpiScid;
import gov.nasa.jpl.ammos.asec.kmc.api.sadb.IDbSession;
import gov.nasa.jpl.ammos.asec.kmc.api.sadb.IKmcDao;
import gov.nasa.jpl.ammos.asec.kmc.format.SaCsvInput;
import gov.nasa.jpl.ammos.asec.kmc.format.SaCsvOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static gov.nasa.jpl.ammos.asec.kmc.sadb.KmcDao.*;

/**
 * SADB REST Controller
 *
 */
@RestController
public class SaController {

    private static Logger  LOG = LoggerFactory.getLogger(SaController.class);
    private final  IKmcDao dao;

    @Autowired
    public SaController(IKmcDao dao) {
        this.dao = dao;
    }

    /**
     * Convenience class for reset ARSN operation
     */
    private static class IdArsn implements Serializable {
        public SpiScid id;
        @JsonSerialize(using = ByteArraySerializer.class)
        @JsonDeserialize(using = ByteArrayDeserializer.class)
        public byte[]  arsn;
        public Short   arsnLen;
        public Short   arsnw;
    }

    /**
     * Convenience class for reset IV operation
     */
    private static class IdIv implements Serializable {
        public SpiScid id;
        @JsonSerialize(using = ByteArraySerializer.class)
        @JsonDeserialize(using = ByteArrayDeserializer.class)
        public byte[]  iv;
        public Short   ivLen;
    }

    /**
     * Convenience class for rekey operation
     */
    private static class Rekey implements Serializable {
        public SpiScid id;
        public String  ekid;
        public String  akid;
    }

    @GetMapping("/api/sa")
    public List<SecAssn> getSa(@RequestParam(required = false) Short scid,
                               @RequestParam(required = false) Integer spi, HttpServletRequest request) throws KmcException {
        LOG.info("{} retrieving all SAs", request.getRemoteAddr());
        if (spi != null && scid != null) {
            return new ArrayList<>(Arrays.asList(dao.getSa(new SpiScid(spi, scid))));
        }
        List<SecAssn> sas = dao.getSas();
        if (scid != null) {
            sas = sas.stream().filter(sa -> sa.getScid().equals(scid)).collect(Collectors.toList());
        }
        if (spi != null) {
            sas = sas.stream().filter(sa -> sa.getSpi().equals(spi)).collect(Collectors.toList());
        }
        return sas;
    }

    @PutMapping("/api/sa")
    public SecAssn putSa(@RequestBody SecAssn sa, HttpServletRequest request) throws KmcException {
        LOG.info("{} creating SA ({}/{})", request.getRemoteAddr(), sa.getSpi(), sa.getScid());
        SecAssn newSa = dao.createSa(sa);
        LOG.info("{} created SA ({}/{})", request.getRemoteAddr(), sa.getSpi(), sa.getScid());
        return newSa;
    }

    @PostMapping(value = "/api/sa", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public SecAssn postSa(@RequestBody SecAssn sa, HttpServletRequest request) throws KmcException {
        LOG.info("{} updating SA ({}/{})", request.getRemoteAddr(), sa.getSpi(), sa.getScid());
        checkEncryption(sa);
        checkAuthentication(sa);
        SecAssn original = dao.getSa(sa.getId());
        try (IDbSession dbSession = dao.newSession()) {
            dbSession.beginTransaction();
            if (original.getSaState() != sa.getSaState()) {
                switch (sa.getSaState()) {
                    case SA_OPERATIONAL:
                        dao.startSa(dbSession, sa.getId(), true);
                        break;
                    case SA_KEYED:
                        dao.stopSa(dbSession, sa.getId());
                        break;
                    default: // handles SA_UNKEYED and SA_EXPIRED
                        dao.expireSa(dbSession, sa.getId());
                        break;
                }
            }
            dao.updateSa(dbSession, sa);
            dbSession.commit();
        } catch (KmcException e) {
            throw e;
        } catch (Exception e) {
            throw new KmcException(e);
        }
        LOG.info("{} updated SA ({}/{})", request.getRemoteAddr(), sa.getSpi(), sa.getScid());
        return dao.getSa(sa.getId());
    }

    private void checkAuthentication(SecAssn sa) throws KmcException {
        if (sa.getServiceType() == ServiceType.AUTHENTICATION) {
            if (sa.getAcs() == null) {
                throw new KmcException("When service type is  AUTHENTICATION, AKID and ACS are" + " required");
            }
            int acs = 0;
            for (byte b : sa.getAcs()) {
                acs = (acs << 8) + (b & 0xff);
            }
            if ((sa.getSaState() != SA_EXPIRE || sa.getSaState() != SA_UNKEYED) && (acs == 0 || (sa.getAkid() == null || sa.getAkid().isEmpty()))) {
                throw new KmcException("When service type is  AUTHENTICATION, AKID and ACS are" + " required");
            }
        }
    }

    private void checkEncryption(SecAssn sa) throws KmcException {
        if (sa.getServiceType() == ServiceType.ENCRYPTION || sa.getServiceType() == ServiceType.AUTHENTICATED_ENCRYPTION) {
            if (sa.getEcs() == null) {
                throw new KmcException("When service type is ENCRYPTION or AUTHENTICATED_ENCRYPTION, EKID and ECS " +
                        "are required");
            }
            int ecs = 0;
            for (byte b : sa.getEcs()) {
                ecs = (ecs << 8) + (b & 0xff);
            }
            if ((sa.getSaState() != SA_EXPIRE || sa.getSaState() != SA_UNKEYED) && (ecs == 0 || (sa.getEkid() == null || sa.getEkid().isEmpty()))) {
                throw new KmcException("When service type is ENCRYPTION or AUTHENTICATED_ENCRYPTION, EKID and ECS " +
                        "are required");
            }
        }
    }

    @PostMapping(value = "/api/sa/start", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SecAssn startSa(@RequestBody SpiScid id, HttpServletRequest request) throws KmcException {
        LOG.info("{} starting SA ({}/{})", request.getRemoteAddr(), id.getSpi(), id.getScid());
        SecAssn sa = dao.startSa(id, true);
        LOG.info("{} started SA ({}/{})", request.getRemoteAddr(), id.getSpi(), id.getScid());
        return sa;
    }

    @PostMapping(value = "/api/sa/stop", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SecAssn stopSa(@RequestBody SpiScid id, HttpServletRequest request) throws KmcException {
        LOG.info("{} stopping SA ({}/{})", request.getRemoteAddr(), id.getSpi(), id.getScid());
        SecAssn sa = dao.stopSa(id);
        LOG.info("{} stopped SA ({}/{})", request.getRemoteAddr(), id.getSpi(), id.getScid());
        return sa;
    }

    @PostMapping(value = "/api/sa/expire", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SecAssn expireSa(@RequestBody SpiScid id, HttpServletRequest request) throws KmcException {
        LOG.info("{} expiring SA ({}/{})", request.getRemoteAddr(), id.getSpi(), id.getScid());
        SecAssn sa = dao.expireSa(id);
        LOG.info("{} expired SA ({}/{})", request.getRemoteAddr(), id.getSpi(), id.getScid());
        return sa;
    }

    @PostMapping(value = "/api/sa/arsn", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> resetArsn(@RequestBody IdArsn idArsn, HttpServletRequest request) throws KmcException {
        LOG.info("{} resetting ARSN on SA ({}/{})", request.getRemoteAddr(), idArsn.id.getSpi(), idArsn.id.getScid());
        ObjectNode respBody = mapper.createObjectNode();
        SecAssn    sa       = dao.getSa(idArsn.id);
        if (sa != null) {
            try {
                if (idArsn.arsn.length < idArsn.arsnLen) {
                    int    diff    = idArsn.arsnLen - idArsn.arsn.length;
                    byte[] newArsn = new byte[idArsn.arsnLen];
                    System.arraycopy(idArsn.arsn, 0, newArsn, diff, idArsn.arsn.length);
                    idArsn.arsn = newArsn;
                    respBody.withArray("messages").add("Array left padded with " + diff + " bytes");
                } else if (idArsn.arsn.length > idArsn.arsnLen) {
                    respBody.put("status", "error");
                    respBody.withArray("messages").add("ARSN is larger than ARSN length in bytes");
                    return ResponseEntity.badRequest().body(respBody);
                }
                sa.setArsn(idArsn.arsn);
                sa.setArsnLen(idArsn.arsnLen);
                sa.setArsnw(idArsn.arsnw);
                dao.updateSa(sa);
                respBody.put("status", "success");
                LOG.info("{} reset ARSN on SA ({}/{})", request.getRemoteAddr(), idArsn.id.getSpi(),
                        idArsn.id.getScid());
                return ResponseEntity.ok().body(respBody);
            } catch (IllegalArgumentException e) {
                respBody.put("status", "error");
                respBody.withArray("messages").add("ARSN input not a valid hex string");
            }
        }
        respBody.put("status", "error");
        respBody.withArray("messages").add("An unknown error occurred");
        LOG.info("{} failed to reset ARSN on SA ({}/{})", request.getRemoteAddr(), idArsn.id.getSpi(),
                idArsn.id.getScid());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respBody);
    }

    @DeleteMapping("/api/sa")
    public void deleteSa(@RequestBody List<SpiScid> ids, HttpServletRequest request) throws KmcException {
        LOG.info("what");
        for (SpiScid id : ids) {
            LOG.info("{} deleting SA ({}/{})", request.getRemoteAddr(), id.getSpi(), id.getScid());
            dao.deleteSa(id);
            LOG.info("{} deleted SA ({}/{})", request.getRemoteAddr(), id.getSpi(), id.getScid());
        }
    }

    @PostMapping(value = "/api/sa/create")
    public ResponseEntity<ObjectNode> bulkCreate(@RequestParam("file") MultipartFile file, @RequestParam(name =
            "force", defaultValue = "false") boolean force, HttpServletRequest request) throws IOException,
                                                                                               KmcException {
        LOG.info("{} creating SAs from file", request.getRemoteAddr());
        ObjectNode resp   = mapper.createObjectNode();
        boolean    errors = false;
        SaCsvInput input  = new SaCsvInput();
        int        count  = 0;
        int        errs   = 0;
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<SecAssn> sas = input.parseCsv(reader);
            try (IDbSession session = dao.newSession()) {
                for (SecAssn sa : sas) {
                    session.beginTransaction();
                    try {
                        SecAssnValidator.validate(sa);
                        SecAssn exists = dao.getSa(sa.getId());
                        if (exists != null && force) {
                            dao.deleteSa(session, sa.getId());
                            session.flush();
                        }
                        dao.createSa(session, sa);
                        count++;
                    } catch (KmcException e) {
                        errors = true;
                        errs++;
                        resp.withArray("messages").add(e.getMessage());
                        session.rollback();
                    } finally {
                        if (session.isActive()) {
                            session.commit();
                        }
                    }
                }
            } catch (KmcException e) {
                handleException(e);
            } catch (Exception e) {
                handleException(e);
            }
        }
        HttpStatus status = HttpStatus.CREATED;
        if (!errors) {
            resp.put("status", "success");
            LOG.info("{} created {} SAs from file", request.getRemoteAddr(), count);
        } else {
            status = HttpStatus.BAD_REQUEST;
            resp.put("status", "error");
            LOG.info("{} failed to create SAs from file with {} errors", request.getRemoteAddr(), errs);
        }
        return new ResponseEntity<>(resp, status);
    }

    @GetMapping(value = "/api/sa/csv")
    public ResponseEntity<String> downloadCsv(HttpServletRequest request) throws KmcException {
        LOG.info("{} downloading SAs as CSV", request.getRemoteAddr());
        SaCsvOutput   out = new SaCsvOutput(true);
        List<SecAssn> sas = dao.getSas();
        StringWriter  w   = new StringWriter();
        try (PrintWriter pw = new PrintWriter(w)) {
            out.print(pw, sas);
        }
        SimpleDateFormat sdf      = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String           fileName = "SADB_" + sdf.format(new Date()) + ".csv";
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).header("X-Suggested-Filename", fileName).body(w.toString());
    }

    @PostMapping(value = "/api/sa/iv", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> resetIv(@RequestBody IdIv idIv) throws KmcException {
        ObjectNode respBody = mapper.createObjectNode();
        try (IDbSession dbSession = dao.newSession()) {
            try {
                dbSession.beginTransaction();
                SecAssn sa = dao.getSa(dbSession, idIv.id);
                if (sa != null) {
                    try {
                        if (idIv.iv.length < idIv.ivLen) {
                            int    diff  = idIv.ivLen - idIv.iv.length;
                            byte[] newIv = new byte[idIv.ivLen];
                            System.arraycopy(idIv.iv, 0, newIv, diff, idIv.iv.length);
                            idIv.iv = newIv;
                            respBody.withArray("messages").add("Array left padded with " + diff + " bytes");
                        } else if (idIv.iv.length > idIv.ivLen) {
                            respBody.put("status", "error");
                            respBody.withArray("messages").add("IV is larger than IV length in bytes");
                            return ResponseEntity.badRequest().body(respBody);
                        }
                        sa.setIv(idIv.iv);
                        sa.setIvLen(idIv.ivLen);
                        dao.updateSa(dbSession, sa);
                        respBody.put("status", "success");
                        return ResponseEntity.ok().body(respBody);
                    } catch (IllegalArgumentException e) {
                        respBody.put("status", "error");
                        respBody.withArray("messages").add("IV input not a valid Base64 string");
                    }
                }
            } finally {
                dbSession.commit();
            }
        } catch (Exception e) {
            handleException(e);
        }

        respBody.put("status", "error");
        respBody.withArray("messages").add("An unknown error occurred");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respBody);
    }

    @PostMapping(value = "/api/sa/key", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> rekeySa(@RequestBody Rekey rekey, HttpServletRequest request) throws KmcException {
        LOG.info("{} Rekeying SA {}/{}", request.getRemoteAddr(), rekey.id.getSpi(), rekey.id.getScid());
        try (IDbSession dbSession = dao.newSession()) {
            try {
                dbSession.beginTransaction();
                SecAssn sa = dao.getSa(dbSession, rekey.id);
                if (rekey.ekid != null && !rekey.ekid.equals(sa.getEkid())) {
                    dao.rekeySaEnc(dbSession, sa.getId(), rekey.ekid, sa.getEcs(), sa.getEcsLen());
                    dbSession.flush();
                }
                if (rekey.akid != null && !rekey.akid.equals(sa.getAkid())) {
                    dao.rekeySaAuth(dbSession, sa.getId(), rekey.akid, sa.getAcs(), sa.getAcsLen());
                    dbSession.flush();
                }
            } finally {
                dbSession.commit();
            }
            LOG.info("{} Rekeyed SA {}/{}", request.getRemoteAddr(), rekey.id.getSpi(), rekey.id.getScid());
        } catch (Exception e) {
            handleException(e);
        }

        return ResponseEntity.ok().body(mapper.createObjectNode().put("status", "success"));
    }

    @GetMapping(value = "/api/status")
    public ResponseEntity<JsonNode> status() throws KmcException {
        boolean    status = dao.status();
        ObjectNode node   = mapper.createObjectNode();
        node.put("status", status ? "ok" : "database down");
        return new ResponseEntity<>(node, status ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE);
    }

    @GetMapping(value = "/api/health")
    public String health() {
        // This method will simply return HTTP 200 status with the following string
        return "Service is UP\n";
    }

    private static ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler({KmcException.class, Exception.class})
    public ResponseEntity<Object> handleException(Exception e) {
        LOG.error("An exception occurred", e);
        ObjectNode node = mapper.createObjectNode();
        node.put("status", "error");
        node.withArray("messages").add(e.getMessage());
        ResponseEntity<Object> entity = new ResponseEntity<>(node, HttpStatus.BAD_REQUEST);
        return entity;
    }
}
