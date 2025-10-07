package ru.practicum.statsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statsdto.HitDto;
import ru.practicum.statsdto.Stat;
import ru.practicum.statsservice.exception.InvalidPeriodException;
import ru.practicum.statsservice.mapper.AppMapper;
import ru.practicum.statsservice.mapper.HitMapper;
import ru.practicum.statsservice.model.App;
import ru.practicum.statsservice.storage.AppRepo;
import ru.practicum.statsservice.storage.StatsRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class StatsServiceImpl implements StatsService {
    private final AppRepo appRepo;
    private final StatsRepo statsRepo;

    @Override
    public void saveRequest(HitDto dto) {
        App app = getOrCreate(dto.getApp());

        statsRepo.save(HitMapper.toHit(dto, app));
        log.info("Сохранён запрос с IP = {} по URL = {}", dto.getIp(), dto.getUri());
    }

    @Override
    public List<Stat> getHits(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        checkPeriod(start, end);

        return get(start, end, uris, unique);
    }

    private App getOrCreate(String appName) {
        return appRepo.findByName(appName)
                .orElseGet(() -> appRepo.save(AppMapper.toApp(appName)));
    }

    private List<Stat> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris.isEmpty()) {
            if (unique) {
                log.info("Запросить статистику, сгенерированную по URIs ={} для уникальных IP-адресов", uris);
                return statsRepo.getUniqueIpStatNoUri(start, end);
            } else {
                log.info("Запросить статистику, сгенерированную по URIs ={} для не уникального IP", uris);
                return statsRepo.getNotUniqueIpStatNoUri(start, end);
            }
        } else {
            if (unique) {
                log.info("Запросить статистику, сгенерированную по URIs ={} для уникального IP", uris);
                return statsRepo.getUniqueIpStat(start, end, uris);
            } else {
                log.info("Запросить статистику, сгенерированную по URIs ={} для не уникального IP", uris);
                return statsRepo.getNotUniqueIpStat(start, end, uris);
            }
        }
    }

    private void checkPeriod(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new InvalidPeriodException("Окончание периода поиска не может быть раньше начала");
        }
    }
}