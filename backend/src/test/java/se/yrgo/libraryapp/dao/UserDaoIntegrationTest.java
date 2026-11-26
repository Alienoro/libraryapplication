package se.yrgo.libraryapp.dao;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.yrgo.libraryapp.entities.LoginInfo;
import se.yrgo.libraryapp.entities.User;
import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@Testcontainers

public class UserDaoIntegrationTest {

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.29");

    private static DataSource ds;
    @BeforeAll
    static void initDataSource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(mysql.getJdbcUrl());
        hikariConfig.setUsername(mysql.getUsername());
        hikariConfig.setPassword(mysql.getPassword());
        hikariConfig.setDriverClassName(mysql.getDriverClassName());

        ds = new HikariDataSource(hikariConfig);

        Flyway.configure()
                .dataSource(ds)
                .load()
                .migrate();
    }
    @Test
    void getUserById() {
// this data comes from the test migration files
        final String username = "test";
        final UserId userId = UserId.of(1);
        UserDao userDao = new UserDao(ds);
        Optional<User> maybeUser = userDao.get(Integer.toString(userId.getId()));
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.get().getName()).isEqualTo(username);
        assertThat(maybeUser.get().getId()).isEqualTo(userId);
    }
    @Test
    void getUser_notFound() {
        UserDao userDao = new UserDao(ds);
        Optional<User> maybeUser = userDao.get("99999");

        assertThat(maybeUser).isEmpty();
    }
    @Test
    void registerAndGetUser() {
        UserDao userDao = new UserDao(ds);
        boolean success = userDao.register("newhampususer", "Hampus User", "hampyhash");

        assertThat(success).isTrue();
        Optional<LoginInfo> loginInfo = userDao.getLoginInfo("newhampususer");
        assertThat(loginInfo).isPresent();
    }

    @Test
    void isNameAvailable() {
        UserDao userDao = new UserDao(ds);

        boolean available = userDao.isNameAvailable("brandnewhampususer");
        assertThat(available).isTrue();

        userDao.register("brandnewhampususer", "Hampus NewAccount", "hampusstarkalösenord");

        boolean notAvailable = userDao.isNameAvailable("brandnewhampususer");
        assertThat(notAvailable).isFalse();
    }
    @Test
    void IsNameNotAvailable() {
        UserDao userDao = new UserDao(ds);

        boolean firstInsert = userDao.register("upptagenhampususer", "Snälla Hampus", "hampus123");
        assertThat(firstInsert).isTrue();

        boolean secondInsert = userDao.register("upptagenhampususer", "Hampus Onda Tvilling", "ondahampus123");
        assertThat(secondInsert).isFalse();
    }
}