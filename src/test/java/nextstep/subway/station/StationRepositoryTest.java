package nextstep.subway.station;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.domain.StationRepository;
import org.hibernate.annotations.Source;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.repository.query.Param;

@DataJpaTest
public class StationRepositoryTest {
    @Autowired
    StationRepository stationRepository;
    private final Station 강남역 = new Station("강남역");
    private final Station 판교역 = new Station("판교역");
    private final Station 정자역 = new Station("정자역");
    private final Station 광교역 = new Station("광교역");

    @BeforeEach
    void setUp() {
        stationRepository.save(강남역);
        stationRepository.save(판교역);
        stationRepository.save(정자역);
        stationRepository.save(광교역);
    }

    @Test
    void Id에_해당하는_모든_역_조회() {
        List<Long> ids = Arrays.asList(1L, 2L);
        List<Station> stations = stationRepository.findAllByIdIsIn(ids);
        assertThat(stations).containsExactly(강남역, 판교역);
    }
}