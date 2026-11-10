package se.yrgo.libraryapp.validators;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RealNameTest {

    @ParameterizedTest
    @CsvSource({
            "Hampus Ram",
            "Karl-Johan",
            "Tobias Sebastian Tuvebäck"
    })
    void testValidRealNames(String name) {
        assertThat(RealName.validate(name)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "Skit User",
            "Penis Pojken",
            "Bajs-Berget"
    })
    void testInvalidRealNamesWithBadWords(String name) {
        assertThat(RealName.validate(name)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testRealNameNullAndEmpty(String name) {
        assertThat(RealName.validate(name)).isFalse();
    }
}
