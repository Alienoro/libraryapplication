package se.yrgo.libraryapp.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import io.jooby.JoobyTest;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

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
public class RegisterIntegrationTest {

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
    
    private static HttpClient newClient() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager()).build();
    }

    private static HttpResponse<String> postJson(
            HttpClient client, int serverPort, String path, String json)
            throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + serverPort + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(req, BodyHandlers.ofString());
    }

    @Test
    void registerWorks(int serverPort) throws IOException, InterruptedException {
        HttpClient client = newClient();
        String body = "{\"name\":\"hampus_reg1\",\"realName\":\"Hampus Ram\",\"password\":\"ram1234\"}";

        HttpResponse<String> resp = postJson(client, serverPort, "/register", body);
        assertThat(resp.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(resp.body()).isEqualTo("true");
    }

    @Test
    void duplicateUsernameFails(int serverPort) throws IOException, InterruptedException {
        HttpClient client = newClient();
        String body = "{\"name\":\"hampus_dup1\",\"realName\":\"Hampus Ram\",\"password\":\"ram1234\"}";

        assertThat(postJson(client, serverPort, "/register", body).body()).isEqualTo("true");
        assertThat(postJson(client, serverPort, "/register", body).body()).isEqualTo("false");
    }

    @Test
    void registerAndDuplicateFails(int serverPort) throws IOException, InterruptedException {
        HttpClient client = newClient();
        String body = "{\"name\":\"hampus1\",\"realName\":\"Hampus Ram\",\"password\":\"hampe1234\"}";

        HttpResponse<String> first = postJson(client, serverPort, "/register", body);
        assertThat(first.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(first.body()).isEqualTo("true");

        HttpResponse<String> duplicate = postJson(client, serverPort, "/register", body);
        assertThat(duplicate.statusCode()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(duplicate.body()).isEqualTo("false");
    }


}