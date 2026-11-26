package se.yrgo.libraryapp.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import io.jooby.JoobyTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.yrgo.libraryapp.App;

@Tag("integration")
@Testcontainers
@JoobyTest(value = App.class, factoryMethod = "createApp")
public class LoginLogoutIntegrationTest {

    @Container
    private static final MySQLContainer<?> mySQLContainer =
            new MySQLContainer<>("mysql:8.0.29");

    public static App createApp() {
        System.setProperty("db.url", mySQLContainer.getJdbcUrl());
        System.setProperty("db.user", mySQLContainer.getUsername());
        System.setProperty("db.password", mySQLContainer.getPassword());
        return new App();
    }

    @AfterAll
    static void clearDbProperties() {
        System.clearProperty("db.url");
        System.clearProperty("db.user");
        System.clearProperty("db.password");
    }

    @Test
    void alwaysOkToLogout(int serverPort) throws IOException, InterruptedException {
        HttpClient client = newClient();
        HttpResponse<String> resp = get(client, serverPort, "/logout");

        assertThat(resp.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(resp.body()).isEmpty();
    }

    private static HttpClient newClient() {
        return HttpClient.newBuilder()
                .cookieHandler(new CookieManager())
                .build();
    }

    private static HttpResponse<String> get(HttpClient client, int serverPort, String path)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(
                URI.create("http://localhost:" + serverPort + path)).build();
        return client.send(req, BodyHandlers.ofString());
    }

    private static HttpResponse<String> postJson(
            HttpClient client, int serverPort, String path, String json)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(
                        URI.create("http://localhost:" + serverPort + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(req, BodyHandlers.ofString());
    }

    @Test
    void loginAndLogoutSucceeds(int serverPort) throws IOException, InterruptedException {
        HttpClient client = newClient();

        HttpResponse<String> loginResp = login(client, serverPort, "test2", "yrgoP4ssword");
        assertThat(loginResp.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(loginResp.body()).isNotEqualTo("[]");

        HttpResponse<String> checkResp = get(client, serverPort, "/login/check");
        assertThat(checkResp.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(checkResp.body()).isNotEqualTo("[]");

        HttpResponse<String> logoutResp = get(client, serverPort, "/logout");
        assertThat(logoutResp.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);

        HttpResponse<String> afterLogoutCheck = get(client, serverPort, "/login/check");
        assertThat(afterLogoutCheck.body()).isEqualTo("[]");
    }

    @Test
    void loginFailsWithInvalidCredentials(int serverPort) throws IOException, InterruptedException {
        HttpClient client = newClient();

        HttpResponse<String> resp = login(client, serverPort, "test2", "fellosenord");

        assertThat(resp.statusCode()).isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
        assertThat(resp.body()).isEqualTo("[]");
    }

    private static HttpResponse<String> login(
            HttpClient client, int serverPort, String username, String password)
            throws IOException, InterruptedException {
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        return postJson(client, serverPort, "/login", json);
    }
}