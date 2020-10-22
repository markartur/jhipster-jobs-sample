package com.jhipsterdemo.company.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import com.jhipsterdemo.company.web.rest.TestUtil;

public class AccommodationTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Accommodation.class);
        Accommodation accommodation1 = new Accommodation();
        accommodation1.setId("id1");
        Accommodation accommodation2 = new Accommodation();
        accommodation2.setId(accommodation1.getId());
        assertThat(accommodation1).isEqualTo(accommodation2);
        accommodation2.setId("id2");
        assertThat(accommodation1).isNotEqualTo(accommodation2);
        accommodation1.setId(null);
        assertThat(accommodation1).isNotEqualTo(accommodation2);
    }
}
