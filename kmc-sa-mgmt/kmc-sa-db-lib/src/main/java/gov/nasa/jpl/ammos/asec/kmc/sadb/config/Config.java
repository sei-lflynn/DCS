package gov.nasa.jpl.ammos.asec.kmc.sadb.config;

import org.apache.commons.configuration2.CompositeConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.BasePathLocationStrategy;
import org.apache.commons.configuration2.io.ClasspathLocationStrategy;
import org.apache.commons.configuration2.io.CombinedLocationStrategy;
import org.apache.commons.configuration2.io.FileLocationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for KMC CLI
 */
public class Config extends CompositeConfiguration {

    private static boolean SYS_OVERRIDES_ENABLED = true;

    public static void setSysOverridesEnabled(boolean enabled) {
        SYS_OVERRIDES_ENABLED = enabled;
    }

    private static final Logger LOG                = LoggerFactory.getLogger(Config.class);
    // Config keys
    public static final  String DB_AUTH_USER       = "db.auth.user";
    public static final  String DB_AUTH_PASS       = "db.auth.pass";
    public static final  String DB_CONN_STRING     = "db.conn.string";
    public static final  String DB_HOST            = "db.host";
    public static final  String DB_PORT            = "db.port";
    public static final  String DB_SCHEMA          = "db.schema";
    public static final  String DB_KEYSTORE        = "db.keystore";
    public static final  String DB_KEYSTORE_PASS   = "db.keystore.pass";
    public static final  String DB_TRUSTSTORE      = "db.truststore";
    public static final  String DB_TRUSTSTORE_PASS = "db.truststore.pass";
    public static final  String DB_TLS             = "db.tls";
    public static final  String DB_MTLS            = "db.mtls";

    // Environment variable overrides
    public static final String ENV_DB_USER            = "DB_USER";
    public static final String ENV_DB_PASS            = "DB_PASS";
    public static final String ENV_DB_CONN_STRING     = "DB_CONN_STRING";
    public static final String ENV_DB_HOST            = "DB_HOST";
    public static final String ENV_DB_PORT            = "DB_PORT";
    public static final String ENV_DB_SCHEMA          = "DB_SCHEMA";
    public static final String ENV_DB_KEYSTORE        = "DB_KEYSTORE";
    public static final String ENV_DB_KEYSTORE_PASS   = "DB_KEYSTORE_PASS";
    public static final String ENV_DB_TRUSTSTORE      = "DB_TRUSTSTORE";
    public static final String ENV_DB_TRUSTSTORE_PASS = "DB_TRUSTSTORE_PASS";
    public static final String ENV_DB_TLS             = "DB_TLS";
    public static final String ENV_DB_MTLS            = "DB_MTLS";
    public static final String NONE                   = "none";
    public static final String KMC_OVERRIDE_CONFIG    = "KMC_OVERRIDE_CONFIG";

    private String  user;
    private String  pass;
    private String  conn;
    private String  host;
    private String  port;
    private String  schema;
    private String  keystore;
    private String  keystorePass;
    private String  truststore;
    private String  truststorePass;
    private Boolean useTls;
    private Boolean useMtls;
    private Boolean overrideJvmTrustore = true;

    public Config(final String basepath, final String filename) {
        super();
        Parameters parameters = new Parameters();
        try {
            List<FileLocationStrategy> strategies = Arrays.asList(new BasePathLocationStrategy(),
                    new ClasspathLocationStrategy());
            FileLocationStrategy strategy = new CombinedLocationStrategy(strategies);
            FileBasedConfigurationBuilder<PropertiesConfiguration> asecConfig =
                    new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                            .configure(
                                    parameters
                                            .properties()
                                            .setLocationStrategy(strategy)
                                            .setBasePath(basepath)
                                            .setFileName(filename));

            FileBasedConfigurationBuilder<PropertiesConfiguration> builderDefaults =
                    new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                            .configure(
                                    parameters
                                            .properties()
                                            .setLocationStrategy(strategy)
                                            .setFileName("etc/defaults.properties"));

            FileBasedConfigurationBuilder<PropertiesConfiguration> builderConfig =
                    new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                            .configure(
                                    parameters
                                            .properties()
                                            .setLocationStrategy(strategy)
                                            .setFileName("etc/" + filename));

            FileBasedConfigurationBuilder<PropertiesConfiguration> overrideConfig =
                    new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                            .configure(
                                    parameters
                                            .properties()
                                            .setLocationStrategy(strategy)
                                            .setFileName(filename));


            if (SYS_OVERRIDES_ENABLED) {
                LOG.info("{} env var found, making {} primary config", KMC_OVERRIDE_CONFIG,
                        System.getenv(KMC_OVERRIDE_CONFIG));
                FileBasedConfigurationBuilder<PropertiesConfiguration> devOverride =
                        new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
                                .configure(
                                        parameters
                                                .properties()
                                                .setLocationStrategy(strategy)
                                                .setFileName(System.getenv(KMC_OVERRIDE_CONFIG)));
                this.addConfiguration(devOverride.getConfiguration());
            }

            if (System.getProperty("KMC_UNIT_TEST") == null) {
                try {
                    LOG.debug("Loading ASEC config under {}/{}", basepath, filename);
                    this.addConfiguration(asecConfig.getConfiguration());
                } catch (ConfigurationException e) {
                    LOG.warn("ASEC config not found under {}/{}", basepath, filename);
                }
            }

            try {
                LOG.debug("Loading config from classpath under ./etc/{}", filename);
                this.addConfiguration(builderConfig.getConfiguration());
            } catch (ConfigurationException e) {
                LOG.warn("Classpath config not found under ./etc/{}", filename);
                try {
                    LOG.debug("Loading config from classpath under ./{}", filename);
                    this.addConfiguration(overrideConfig.getConfiguration());
                } catch (ConfigurationException e1) {
                    LOG.warn("Classpath config not found under ./{}, {}", filename, e.getMessage());
                }
            }
            LOG.debug("Loading classpath default config under ./etc/defaults.properties");
            this.addConfiguration(builderDefaults.getConfiguration());

        } catch (ConfigurationException e) {
            throw new RuntimeException("Configuration error", e);
        }

        this.user = getString(DB_AUTH_USER);
        if (System.getenv(ENV_DB_USER) != null) {
            LOG.info("{} env var found, overriding {} user", ENV_DB_USER, DB_AUTH_USER);
            this.user = System.getenv(ENV_DB_USER);
        }
        this.pass = getString(DB_AUTH_PASS);
        if (System.getenv(ENV_DB_PASS) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_PASS, DB_AUTH_PASS);
            this.pass = System.getenv(ENV_DB_PASS);
        }
        this.conn = getString(DB_CONN_STRING);
        if (System.getenv(ENV_DB_CONN_STRING) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_CONN_STRING, DB_CONN_STRING);
            this.conn = System.getenv(ENV_DB_CONN_STRING);
        }
        this.host = getString(DB_HOST);
        if (System.getenv(ENV_DB_HOST) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_HOST, DB_HOST);
            this.host = System.getenv(ENV_DB_HOST);
        }
        this.port = getString(DB_PORT);
        if (System.getenv(ENV_DB_PORT) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_PORT, DB_PORT);
            this.port = System.getenv(ENV_DB_PORT);
        }
        this.schema = getString(DB_SCHEMA);
        if (System.getenv(ENV_DB_SCHEMA) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_SCHEMA, DB_SCHEMA);
            this.schema = System.getenv(ENV_DB_SCHEMA);
        }
        this.keystore = getString(DB_KEYSTORE);
        if (System.getenv(ENV_DB_KEYSTORE) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_KEYSTORE, DB_KEYSTORE);
            this.keystore = System.getenv(ENV_DB_KEYSTORE);
        }
        this.keystorePass = getString(DB_KEYSTORE_PASS);
        if (System.getenv(ENV_DB_KEYSTORE_PASS) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_KEYSTORE_PASS, DB_KEYSTORE_PASS);
            this.keystorePass = System.getenv(ENV_DB_KEYSTORE_PASS);
        }
        this.truststore = getString(DB_TRUSTSTORE);
        if (System.getenv(ENV_DB_TRUSTSTORE) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_TRUSTSTORE, DB_TRUSTSTORE);
            this.keystore = System.getenv(ENV_DB_TRUSTSTORE);
        }
        this.truststorePass = getString(DB_TRUSTSTORE_PASS);
        if (System.getenv(ENV_DB_TRUSTSTORE_PASS) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_TRUSTSTORE_PASS, DB_TRUSTSTORE_PASS);
            this.keystore = System.getenv(ENV_DB_TRUSTSTORE_PASS);
        }
        this.useTls = getBoolean(DB_TLS);
        if (System.getenv(ENV_DB_TLS) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_TLS, DB_TLS);
            this.useTls = System.getenv(ENV_DB_TLS).equalsIgnoreCase("true");
        }
        this.useMtls = getBoolean(DB_MTLS);
        if (System.getenv(ENV_DB_MTLS) != null) {
            LOG.info("{} env var found, overriding {}", ENV_DB_MTLS, DB_MTLS);
            this.useMtls = System.getenv(ENV_DB_MTLS).equalsIgnoreCase("true");
        }
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getConn() {
        return conn;
    }

    public void setConn(String conn) {
        this.conn = conn;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public String getTruststore() {
        return truststore;
    }

    public void setTruststore(String truststore) {
        this.truststore = truststore;
    }

    public String getTruststorePass() {
        return truststorePass;
    }

    public void setTruststorePass(String truststorePass) {
        this.truststorePass = truststorePass;
    }

    public Boolean getUseTls() {
        return useTls;
    }

    public void setUseTls(Boolean useTls) {
        this.useTls = useTls;
    }

    public Boolean getUseMtls() {
        return useMtls;
    }

    public void setUseMtls(Boolean useMtls) {
        this.useMtls = useMtls;
    }

    public Boolean getOverrideJvmTrustore() {
        return overrideJvmTrustore;
    }

    public void setOverrideJvmTrustore(Boolean overrideJvmTrustore) {
        this.overrideJvmTrustore = overrideJvmTrustore;
    }
}
