package jp.clip.typinggame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Bootアプリケーションの起動確認テストです。
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:typing_game_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@ActiveProfiles("test")
@DisplayName("アプリケーション起動確認")
class ApplicationTests {

    /**
     * SpringのApplicationContextが正常に起動することを確認します。
     */
    @Test
    @DisplayName("ApplicationContextが起動する")
    void contextLoads() {
    }
}
