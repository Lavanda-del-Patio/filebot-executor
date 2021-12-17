package es.lavanda.filebot.executor.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

private final FilebotService filebotService;
    
    @Scheduled(fixedRate = 5000)
    public void executeSchedule() {
        // filebotService.execute();
    }
}
