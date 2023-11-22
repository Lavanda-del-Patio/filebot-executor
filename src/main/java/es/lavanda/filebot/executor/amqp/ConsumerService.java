package es.lavanda.filebot.executor.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.service.FilebotExecutorService;
import es.lavanda.lib.common.model.FilebotExecutionODTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerService {


    private final FilebotExecutorService filebotExecutorService;

    private static final String FILEBOT_TELEGRAM_RESOLUTION = "filebot-telegram-resolution";


    @RabbitListener(queues = FILEBOT_TELEGRAM_RESOLUTION)
    public void consumeMessageFilebotTelegramResolution(FilebotExecutionODTO filebotExecutionODTO) {
        log.info("Reading message of the queue {}: {}", FILEBOT_TELEGRAM_RESOLUTION, filebotExecutionODTO);
        filebotExecutorService.resolutionTelegramBot(filebotExecutionODTO);
        log.info("Work message finished");
    }
}
