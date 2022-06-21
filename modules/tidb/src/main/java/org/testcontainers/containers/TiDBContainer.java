package org.testcontainers.containers;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author richardnorth
 */
public class TiDBContainer extends JdbcDatabaseContainer<TiDBContainer> {

    public static final String NAME = "tidb";

    private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("pingcap/tidb");

    @Deprecated
    public static final String DEFAULT_TAG = "v6.1.0";

    @Deprecated
    public static final String IMAGE = DEFAULT_IMAGE_NAME.getUnversionedPart();

    public static final String IMAGE_TAG = DEFAULT_TAG;

    public static final Integer TIDB_PORT = 4000;

    private static final int REST_API_PORT = 10080;

    private String databaseName = "test";

    private String username = "root";

    private String password = "";

    /**
     * @deprecated use {@link TiDBContainer (DockerImageName)} instead
     */
    @Deprecated
    public TiDBContainer() {
        this(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
    }

    public TiDBContainer(String dockerImageName) {
        this(DockerImageName.parse(dockerImageName));
    }

    public TiDBContainer(final DockerImageName dockerImageName) {
        super(dockerImageName);
        dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);

        addExposedPorts(TIDB_PORT, REST_API_PORT);

        waitingFor(
            new HttpWaitStrategy()
                .forPath("/status")
                .forPort(REST_API_PORT)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(1))
        );
    }

    @NotNull
    @Override
    protected Set<Integer> getLivenessCheckPorts() {
        return new HashSet<>(getMappedPort(TIDB_PORT));
    }

    @Override
    public String getDriverClassName() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return "com.mysql.cj.jdbc.Driver";
        } catch (ClassNotFoundException e) {
            return "com.mysql.jdbc.Driver";
        }
    }

    @Override
    public String getJdbcUrl() {
        String additionalUrlParams = constructUrlParameters("?", "&");
        return "jdbc:mysql://" + getHost() + ":" + getMappedPort(TIDB_PORT) + "/" + databaseName + additionalUrlParams;
    }

    @Override
    protected String constructUrlForConnection(String queryString) {
        String url = super.constructUrlForConnection(queryString);

        if (!url.contains("useSSL=")) {
            String separator = url.contains("?") ? "&" : "?";
            url = url + separator + "useSSL=false";
        }

        if (!url.contains("allowPublicKeyRetrieval=")) {
            url = url + "&allowPublicKeyRetrieval=true";
        }

        return url;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getTestQueryString() {
        return "SELECT 1";
    }

    @Override
    public TiDBContainer withDatabaseName(final String databaseName) {
        throw new UnsupportedOperationException(
            "The TiDB docker image does not currently support this"
        );
    }

    @Override
    public TiDBContainer withUsername(final String username) {
        throw new UnsupportedOperationException(
            "The TiDB docker image does not currently support this"
        );
    }

    @Override
    public TiDBContainer withPassword(final String password) {
        throw new UnsupportedOperationException(
            "The TiDB docker image does not currently support this"
        );
    }
}