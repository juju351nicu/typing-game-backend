package jp.clip.typinggame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * typingGameバックエンドAPIの起動クラスです。
 */
@SpringBootApplication
public class Application {

    /**
     * アプリケーションを起動します。
     *
     * @param args 起動時のコマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
