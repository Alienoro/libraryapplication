package se.yrgo.libraryapp.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UtilsTest {
    @ParameterizedTest
    @CsvSource({
            "Hello World 123!, hello world ",
            "Test-123, test",
            "HAMPYhampy, hampyhampy"
    })
    void testOnlyLettersAndWhitespace(String input, String expected) {
        assertThat(Utils.onlyLettersAndWhitespace(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "h3ll0 w0rld, hello world",
            "h4mpu5, hampus",
            "b0ss3, bosse"
    })
    void testCleanAndUnLeet(String input, String expected) {
        assertThat(Utils.cleanAndUnLeet(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testOnlyLettersAndWhitespaceWithNullAndEmpty(String input) {
        assertThat(Utils.onlyLettersAndWhitespace(input)).isEqualTo("");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testCleanAndUnLeetWithNullAndEmpty(String input) {
        assertThat(Utils.cleanAndUnLeet(input)).isEqualTo("");
    }
}
