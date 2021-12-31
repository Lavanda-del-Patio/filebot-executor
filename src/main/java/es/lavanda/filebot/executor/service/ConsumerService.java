package es.lavanda.filebot.executor.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import es.lavanda.lib.common.model.FilebotExecutionODTO;
import es.lavanda.lib.common.model.MediaIDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerService {

    private final FilebotService filebotServiceImpl;

    @RabbitListener(queues = "filebot-executor-resolution")
    public void consumeMessageFeedFilms(FilebotExecutionODTO filebotExecutionODTO) {
        log.debug("Reading message of the queue agent-tmdb-feed-films: {}", filebotExecutionODTO);
        filebotServiceImpl.resolution(filebotExecutionODTO);
        log.debug("Work message finished");
    }

}
