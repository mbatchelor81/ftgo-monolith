package com.ftgo.openapi.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PagedResponseTest {

    @Test
    void constructorCalculatesPaginationFields() {
        PagedResponse<String> response = new PagedResponse<>(List.of("a", "b", "c"), 0, 3, 10);

        assertThat(response.getContent()).containsExactly("a", "b", "c");
        assertThat(response.getPage()).isZero();
        assertThat(response.getSize()).isEqualTo(3);
        assertThat(response.getTotalElements()).isEqualTo(10);
        assertThat(response.getTotalPages()).isEqualTo(4);
        assertThat(response.isFirst()).isTrue();
        assertThat(response.isLast()).isFalse();
    }

    @Test
    void lastPageIsDetectedCorrectly() {
        PagedResponse<String> response = new PagedResponse<>(List.of("x"), 3, 3, 10);

        assertThat(response.isFirst()).isFalse();
        assertThat(response.isLast()).isTrue();
    }
}
