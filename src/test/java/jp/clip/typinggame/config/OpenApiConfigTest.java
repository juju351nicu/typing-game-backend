package jp.clip.typinggame.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * OpenAPI設定を確認するテストです。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OpenAPI設定")
class OpenApiConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Swagger UIからBearer tokenを設定できるOpenAPI定義になっていることを確認します。
     *
     * @throws Exception MockMvc実行時に例外が発生した場合
     */
    @Test
    @DisplayName("OpenAPI JSONにBearer認証定義が含まれる")
    void apiDocsContainsBearerAuthSecurityScheme() throws Exception {
        // Swagger UIのAuthorizeボタンでJWTを入力できるよう、OpenAPI JSONのsecuritySchemesを確認します。
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.security[0].bearerAuth").isArray());
    }
}
