package jp.clip.typinggame.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jp.clip.typinggame.repository.ScoreRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScoreRepository scoreRepository;

    @BeforeEach
    void setUp() {
        scoreRepository.deleteAll();
    }

    @Test
    void saveScoreReturnsCreatedScore() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:00:28.00",
                "score", 12,
                "mode", 2,
                "gameRule", "timeAttack",
                "timeLimitSeconds", 60,
                "wpm", 32,
                "accuracy", 96,
                "missCount", 2,
                "correctCharacterCount", 80);

        mockMvc.perform(post("/api/scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.time").value("00:00:28.00"))
                .andExpect(jsonPath("$.score").value(12))
                .andExpect(jsonPath("$.mode").value(2))
                .andExpect(jsonPath("$.gameRule").value("timeAttack"))
                .andExpect(jsonPath("$.timeLimitSeconds").value(60))
                .andExpect(jsonPath("$.wpm").value(32))
                .andExpect(jsonPath("$.accuracy").value(96))
                .andExpect(jsonPath("$.missCount").value(2))
                .andExpect(jsonPath("$.correctCharacterCount").value(80))
                .andExpect(jsonPath("$.date").isString());
    }

    @Test
    void findAllReturnsSavedScores() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:01:10.00",
                "score", 18,
                "mode", 1,
                "gameRule", "normal",
                "wpm", 24,
                "accuracy", 94,
                "missCount", 4,
                "correctCharacterCount", 92);

        mockMvc.perform(post("/api/scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/scores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].time").value("00:01:10.00"))
                .andExpect(jsonPath("$[0].score").value(18))
                .andExpect(jsonPath("$[0].gameRule").value("normal"));
    }

    @Test
    void saveScoreReturnsBadRequestWhenInvalid() throws Exception {
        Map<String, Object> request = Map.of(
                "time", "00:00:28.00",
                "score", -1,
                "mode", 2,
                "gameRule", "timeAttack");

        mockMvc.perform(post("/api/scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
