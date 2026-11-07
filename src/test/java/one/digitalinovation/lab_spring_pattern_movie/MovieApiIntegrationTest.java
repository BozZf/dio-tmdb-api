package one.digitalinovation.lab_spring_pattern_movie;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovieApiIntegrationTest {
    private static final String TEST_KEY = "test-only-key";
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final AtomicReference<UpstreamResponse> RESPONSE = new AtomicReference<>();
    private static final AtomicReference<UpstreamRequest> REQUEST = new AtomicReference<>();
    private static final HttpServer TMDB = startStub();

    @Autowired
    private MockMvc mvc;

    @DynamicPropertySource
    static void configureTmdb(DynamicPropertyRegistry registry) {
        registry.add("tmdb.api.url", () -> "http://127.0.0.1:" + TMDB.getAddress().getPort() + "/3");
        registry.add("tmdb.api.key", () -> TEST_KEY);
        registry.add("spring.cloud.openfeign.client.config.tmdb.readTimeout", () -> 1000);
    }

    @AfterAll
    static void stopStub() {
        TMDB.stop(0);
        EXECUTOR.shutdownNow();
    }

    @Test
    void getsMovieAndPreservesPublicFieldNames() throws Exception {
        respond(200, """
                {
                  "id": 603, "title": "The Matrix", "poster_path": "/matrix.jpg",
                  "original_title": "The Matrix", "original_language": "en",
                  "release_date": "1999-03-31", "backdrop_path": "/backdrop.jpg",
                  "vote_average": 8.2, "vote_count": 100, "genres": [{"id": 28, "name": "Action"}]
                }
                """);

        mvc.perform(get("/movie/{id}", 603))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(603))
                .andExpect(jsonPath("$.posterPath").value("/matrix.jpg"))
                .andExpect(jsonPath("$.originalTitle").value("The Matrix"))
                .andExpect(jsonPath("$.originalLanguage").value("en"))
                .andExpect(jsonPath("$.releaseDate").value("1999-03-31"))
                .andExpect(jsonPath("$.backdropPath").value("/backdrop.jpg"))
                .andExpect(jsonPath("$.voteAverage").value(8.2))
                .andExpect(jsonPath("$.voteCount").value(100))
                .andExpect(jsonPath("$.poster_path").doesNotHaveJsonPath());

        assertThat(REQUEST.get()).isEqualTo(new UpstreamRequest("/3/movie/603", Map.of("api_key", TEST_KEY)));
    }

    @Test
    void searchesWithEncodedQueryAndReturnsOnlyResults() throws Exception {
        String query = "Ação & Ficção + 100%";
        respond(200, """
                {"page":1,"results":[{"id":603,"title":"The Matrix","genre_ids":[28,878]}],"total_pages":2}
                """);

        mvc.perform(get("/search/movie").param("query", query))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(603))
                .andExpect(jsonPath("$[0].genreIds[1]").value(878));

        assertThat(REQUEST.get()).isEqualTo(new UpstreamRequest(
                "/3/search/movie", Map.of("api_key", TEST_KEY, "query", query)));
    }

    @Test
    void returnsAnEmptyArrayWhenThereAreNoResults() throws Exception {
        respond(200, "{\"results\":[]}");

        mvc.perform(get("/search/movie").param("query", "sem resultados"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc", "2147483648"})
    void rejectsInvalidMovieIds(String id) throws Exception {
        respond(500, "{}");

        mvc.perform(get("/movie/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON));

        assertThat(REQUEST.get()).isNull();
    }

    @Test
    void rejectsAMissingQuery() throws Exception {
        respond(500, "{}");

        mvc.perform(get("/search/movie"))
                .andExpect(status().isBadRequest());

        assertThat(REQUEST.get()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsBlankQueries(String query) throws Exception {
        respond(500, "{}");

        mvc.perform(get("/search/movie").param("query", query))
                .andExpect(status().isBadRequest());

        assertThat(REQUEST.get()).isNull();
    }

    @Test
    void returnsNotFoundForAMissingMovie() throws Exception {
        respond(404, "{}");

        mvc.perform(get("/movie/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Filme não encontrado."));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 404, 429, 503})
    void hidesUpstreamErrorDetailsAndCredentials(int upstreamStatus) throws Exception {
        respond(upstreamStatus, "{\"internal\":\"" + TEST_KEY + "\"}");

        mvc.perform(get("/search/movie").param("query", "matrix"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(not(containsString(TEST_KEY))));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{\"results\":null}", "{\"results\":"})
    void rejectsInvalidSearchResponses(String body) throws Exception {
        respond(200, body);

        mvc.perform(get("/search/movie").param("query", "matrix"))
                .andExpect(status().isBadGateway())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON));
    }

    @Test
    void rejectsAnIncompleteMovieResponse() throws Exception {
        respond(200, "{\"title\":\"The Matrix\"}");

        mvc.perform(get("/movie/603"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("Resposta inválida do TMDB."));
    }

    @Test
    void returnsGatewayTimeoutWhenTmdbDoesNotRespondInTime() throws Exception {
        RESPONSE.set(new UpstreamResponse(200, "{}", 3000));

        mvc.perform(get("/movie/603"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON));
    }

    @Test
    void publishesTheOpenApiContractAndSwaggerUi() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/movie/{id}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/search/movie'].get.responses['504']").exists())
                .andExpect(jsonPath("$.paths['/search/movie'].get.responses['200'].content['application/json'].schema.type")
                        .value("array"))
                .andExpect(content().string(not(containsString("api_key"))));

        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    private static void respond(int status, String body) {
        REQUEST.set(null);
        RESPONSE.set(new UpstreamResponse(status, body, 0));
    }

    private static HttpServer startStub() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/3", MovieApiIntegrationTest::serveResponse);
            server.setExecutor(EXECUTOR);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static void serveResponse(HttpExchange exchange) throws IOException {
        try (exchange) {
            Map<String, String> params = Arrays.stream(exchange.getRequestURI().getRawQuery().split("&"))
                    .map(pair -> pair.split("=", 2))
                    .collect(Collectors.toMap(pair -> URLDecoder.decode(pair[0], UTF_8),
                            pair -> URLDecoder.decode(pair[1], UTF_8)));
            REQUEST.set(new UpstreamRequest(exchange.getRequestURI().getPath(), params));

            UpstreamResponse response = RESPONSE.get();
            Thread.sleep(response.delayMs());
            byte[] body = response.body().getBytes(UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(response.status(), body.length);
            exchange.getResponseBody().write(body);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private record UpstreamRequest(String path, Map<String, String> params) {
    }

    private record UpstreamResponse(int status, String body, int delayMs) {
    }
}
