package nextstep.subway.favorite;

import static nextstep.subway.auth.acceptance.AuthAcceptanceSupport.로그인_요청;
import static nextstep.subway.member.MemberAcceptanceTest.회원_생성을_요청;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.favorite.dto.FavoriteRequest;
import nextstep.subway.favorite.dto.FavoriteResponse;
import nextstep.subway.line.acceptance.LineAcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.StationAcceptanceTest;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("즐겨찾기 관련 기능")
class FavoriteAcceptanceTest extends AcceptanceTest {
    private StationResponse 강남역;
    private StationResponse 광교역;
    private LineRequest lineRequest;
    private LineResponse 신분당선;
    private String TOKEN;
    private final String EMAIL = "email@email.com";
    private final String PASSWORD = "password";
    private final int AGE = 20;

    @BeforeEach
    public void setUp() {
        super.setUp();

        // given
        회원_생성을_요청(EMAIL, PASSWORD, AGE);
        TOKEN = 로그인_요청(EMAIL, PASSWORD).jsonPath().getString("accessToken");

        강남역 = StationAcceptanceTest.지하철역_등록되어_있음("강남역").as(StationResponse.class);
        광교역 = StationAcceptanceTest.지하철역_등록되어_있음("광교역").as(StationResponse.class);

        lineRequest = new LineRequest("신분당선", "bg-red-600", 강남역.getId(), 광교역.getId(), 10);
        신분당선 = LineAcceptanceTest.지하철_노선_등록되어_있음(lineRequest).as(LineResponse.class);
    }

    @DisplayName("즐겨찾기를 생성한다.")
    @Test
    void createFavorite() {
        ExtractableResponse<Response> response = 즐겨찾기_생성_요청(TOKEN, new FavoriteRequest(강남역.getId(), 광교역.getId()));

        즐겨찾기_생성됨(response);
    }

    @DisplayName("즐겨찾기를 조회한다.")
    @Test
    void getFavorites() {
        즐겨찾기_생성_요청(TOKEN, new FavoriteRequest(강남역.getId(), 광교역.getId()));

        ExtractableResponse<Response> response = 즐겨찾기_목록_조회_요청(TOKEN);

        List<FavoriteResponse> favorites = response.jsonPath().getList(".", FavoriteResponse.class);
        assertAll(
                () -> assertThat(favorites.get(0).getId()).isEqualTo(1L),
                () -> assertThat(favorites.get(0).getSource().getName()).isEqualTo(강남역.getName()),
                () -> assertThat(favorites.get(0).getTarget().getName()).isEqualTo(광교역.getName())
        );
    }

    @DisplayName("즐겨찾기를 제거한다.")
    @Test
    void deleteFavorite() {
        ExtractableResponse<Response> createResponse = 즐겨찾기_생성_요청(TOKEN, new FavoriteRequest(강남역.getId(), 광교역.getId()));

        ExtractableResponse<Response> response = 즐겨찾기_제거_요청(TOKEN, createResponse);

        즐겨찾기_삭제됨(response);
    }

    public static ExtractableResponse<Response> 즐겨찾기_생성_요청(String accessToken, FavoriteRequest favoriteRequest) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(favoriteRequest)
                .when().post("/favorites")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 즐겨찾기_목록_조회_요청(String accessToken) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/favorites")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 즐겨찾기_제거_요청(String accessToken, ExtractableResponse<Response> response) {
        String uri = response.header("Location");

        return RestAssured
                .given().log().all()
                .auth().oauth2(accessToken)
                .when().delete(uri)
                .then().log().all()
                .extract();
    }

    public static void 즐겨찾기_생성됨(ExtractableResponse<Response> response){
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    public static void 즐겨찾기_삭제됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}