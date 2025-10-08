package ru.practicum.mainservice.event.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.MainCommonUtils;
import ru.practicum.mainservice.event.model.Event;
import ru.practicum.mainservice.event.repository.RequestRepository;
import ru.practicum.stats_client.StatsClient;
import ru.practicum.stats_common.model.ViewStats;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatsClient statsClient;
    private final RequestRepository requestRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value(value = "${app.name}")
    private String appName;

    @Override
    public void addHit(HttpServletRequest request) {
        log.info("Отправлен запрос на регистрацию запроса на сервер статистики с параметрами запроса = {}", request);
        statsClient.addHit(appName, request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.parse(LocalDateTime.now().format(MainCommonUtils.DT_FORMATTER), MainCommonUtils.DT_FORMATTER));
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.info("Отправил запрос на получение статистики на сервер статистики с параметрами " +
                "start = {}, end = {}, uris = {}, unique = {}", start, end, uris, unique);
        ResponseEntity<Object> response = statsClient.getStats(start, end, uris, unique);
        try {
            return Arrays.asList(mapper.readValue(mapper.writeValueAsString(response.getBody()), ViewStats[].class));
        } catch (IOException exception) {
            throw new ClassCastException(exception.getMessage());
        }
    }

    @Override
    public Map<Long, Long> getViews(Set<Event> events) {
        log.info("Был отправлен запрос на получение статистики УНИКАЛЬНЫХ посещений в виде Map<eventId, count> " +
                "для списка событий. ");
        Map<Long, Long> views = new HashMap<>();
        Set<Event> publishedEvents = getPublished(events);
        if (events.isEmpty()) {
            return views;
        }
        Optional<LocalDateTime> minPublishedOn = publishedEvents.stream()
                .map(Event::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);
        if (minPublishedOn.isPresent()) {
            LocalDateTime start = minPublishedOn.get();
            LocalDateTime end = LocalDateTime.now();
            List<String> uris = publishedEvents.stream()
                    .map(Event::getId)
                    .map(id -> ("/events/" + id))
                    .collect(Collectors.toList());

            List<ViewStats> stats = getStats(start, end, uris, true); // <--- ВОТ ИСПРАВЛЕНИЕ

            stats.forEach(stat -> {
                Long eventId = Long.parseLong(stat.getUri()
                        .split("/", 0)[2]);
                views.put(eventId, views.getOrDefault(eventId, 0L) + stat.getHits());
            });
        }
        return views;
    }

    @Override
    public Map<Long, Long> getConfirmedRequests(Set<Event> events) {
        List<Long> eventsId = getPublished(events).stream()
                .map(Event::getId)
                .collect(Collectors.toList());
        Map<Long, Long> requestStats = new HashMap<>();
        if (!eventsId.isEmpty()) {
            requestRepository.getConfirmedRequests(eventsId)
                    .forEach(stat -> requestStats.put(stat.getEventId(), stat.getConfirmedRequests()));
        }
        return requestStats;
    }

    private Set<Event> getPublished(Set<Event> events) {
        return events.stream()
                .filter(event -> event.getPublishedOn() != null)
                .collect(Collectors.toSet());
    }
}