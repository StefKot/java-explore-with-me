package ru.practicum.statsclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.HitsDto;
import ru.practicum.statsdto.Stat;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatsClientImpl implements StatsClient {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${stats-server.uri}")
    private String uri;

    @Value("${stat-client.name}")
    private String name;

    private final RestTemplate restTemplate;

    public StatsClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void saveHit(long eventId, String ip) {
        HitDto dto = HitDtoMapper.fillHit(eventId, ip, name);
        try {
            saveHit(dto);
        } catch (RestClientException ex) {
            log.info(ex.getMessage());
        }
    }

    @Override
    public void saveHits(List<Long> eventIds, String ip) {
        HitsDto dto = HitDtoMapper.fillHits(eventIds, ip, name);
        try {
            saveHit(dto);
        } catch (RestClientException ex) {
            log.info(ex.getMessage());
        }
    }

    @Override
    public List<Stat> getStat(LocalDateTime start, LocalDateTime end, List<Long> eventIds, boolean unique) {
        List<Stat> stats;

        try {
            stats = get(encodeTime(start),
                    encodeTime(end),
                    eventIds.stream().map(e -> "/events/" + e).collect(Collectors.toList()),
                    unique);
        } catch (RestClientException ex) {
            stats = List.of();
            log.info(ex.getMessage());
        }
        log.info("Отправил запрос на статистику eventIds = {}", eventIds);

        return stats;
    }

    @Override
    public List<Stat> getStat(List<Long> eventIds) {
        List<String> uris = fillUris(eventIds);
        List<Stat> stats = fillStat(uris);
        log.info("Отправил запрос на статистику eventIds = {}", eventIds);
        return stats;
    }

    private String encodeTime(LocalDateTime time) {
        String str = time.format(formatter);
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    private List<Stat> get(String start, String end, List<String> uris, boolean unique) {
        String url = UriComponentsBuilder.fromHttpUrl(uri + "/stats")
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .toUriString();

        ResponseEntity<List<Stat>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Stat>>() {
                }
        );

        return response.getBody();
    }

    private List<Stat> get(List<String> uris) {
        String url = UriComponentsBuilder.fromHttpUrl(uri + "/stats")
                .queryParam("uris", uris)
                .toUriString();

        ResponseEntity<List<Stat>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Stat>>() {
                }
        );

        return response.getBody();
    }

    private void saveHit(HitDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HitDto> request = new HttpEntity<>(dto, headers);

        restTemplate.postForEntity(uri + "/hit", request, Void.class);
        log.info("Запрос был отправлен на сохранение запроса IP = {} по URL = {}", dto.getIp(), dto.getUri());
    }

    private void saveHit(HitsDto dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HitsDto> request = new HttpEntity<>(dto, headers);

        restTemplate.postForEntity(uri + "/hits", request, Void.class);
        log.info("Запрос был отправлен на сохранение запроса IP = {} по URLs = {}", dto.getIp(), dto.getUris());
    }

    private List<String> fillUris(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return List.of("/events/");
        } else {
            return eventIds.stream().map(e -> "/events/" + e).collect(Collectors.toList());
        }
    }

    private List<Stat> fillStat(List<String> uris) {
        try {
            return get(uris);
        } catch (RestClientException ex) {
            log.info(ex.getMessage());
            return List.of();
        }
    }
}