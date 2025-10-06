package ru.practicum.statsservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.Stat;
import ru.practicum.statsservice.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    @ResponseStatus(value = HttpStatus.CREATED)
    public void save(@Valid @RequestBody HitDto dto) {
        statsService.saveRequest(dto);
    }

    @GetMapping("/stats")
    public List<Stat> get(@RequestParam(name = "start") LocalDateTime start,
                          @RequestParam(name = "end") LocalDateTime end,
                          @RequestParam(name = "uris", defaultValue = "") List<String> uris,
                          @RequestParam(name = "unique", defaultValue = "false") boolean unique) {
        return statsService.getHits(start, end, uris, unique);
    }
}