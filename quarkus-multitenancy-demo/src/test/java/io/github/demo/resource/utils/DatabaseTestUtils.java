package io.github.demo.resource.utils;

import org.junit.jupiter.api.Assumptions;
import java.net.Socket;

public class DatabaseTestUtils {

    public static boolean isPostgresUp(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void assumePostgresAvailable(String host, int port) {
        Assumptions.assumeTrue(isPostgresUp(host, port),
                "Skipping test — PostgreSQL not running on " + host + ":" + port);
    }
}

