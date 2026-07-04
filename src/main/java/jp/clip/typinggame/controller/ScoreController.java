package jp.clip.typinggame.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jp.clip.typinggame.dto.SaveScoreRequest;
import jp.clip.typinggame.dto.ScoreResponse;
import jp.clip.typinggame.service.ScoreService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScoreResponse save(@Valid @RequestBody SaveScoreRequest request) {
        return scoreService.save(request);
    }

    @GetMapping
    public List<ScoreResponse> findAll() {
        return scoreService.findAll();
    }
}
