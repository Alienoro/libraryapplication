package se.yrgo.libraryapp.validators;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsernameTest {

    @ParameterizedTest
    @ValueSource(strings = {"bosse", "anna123", "a-b-c", "user_name"})
    void correctUsernames(String username) {
        assertThat(Username.validate(username)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a", "name with space", "user@name", "abc!"})
    @NullAndEmptySource
    void incorrectUsernames(String username) {
        assertThat(Username.validate(username)).isFalse();
    }
}
