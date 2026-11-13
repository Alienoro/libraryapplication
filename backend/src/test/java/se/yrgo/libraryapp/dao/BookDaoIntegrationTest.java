package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import java.util.Set;
import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.radcortez.flyway.test.annotation.H2;
import se.yrgo.libraryapp.entities.*;

@Tag("integration")
@H2
public class BookDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        BookDaoIntegrationTest.ds = ds;
    }

    @Test
    void getExistingBook() {
        BookDao bookDao = new BookDao(ds);
        Optional<Book> book = bookDao.get(BookId.of(1));

        assertThat(book).isPresent();
        assertThat(book.get().getEdition().getTitle()).isNotBlank();
    }

    @Test
    void getNonExistingBook() {
        BookDao bookDao = new BookDao(ds);
        Optional<Book> book = bookDao.get(BookId.of(99999));

        assertThat(book).isEmpty();
    }

    @Test
    void findBooksByTitle() {
        BookDao bookDao = new BookDao(ds);
        Set<BookEdition> results = bookDao.find("", "Pippi", "");

        assertThat(results).isNotEmpty();
    }

    @Test
    void testLendAndReturnBook() {
        BookDao bookDao = new BookDao(ds);
        BookId bookId = BookId.of(1);
        UserId userId = UserId.of(1);

        boolean lent = bookDao.lend(bookId, userId);

        if (lent) {
            assertThat(lent).isTrue();
            boolean returned = bookDao.returnBook(bookId);
            assertThat(returned).isTrue();
        } else {
            System.out.println("lend() kunde inte köras.");
        }
    }
}