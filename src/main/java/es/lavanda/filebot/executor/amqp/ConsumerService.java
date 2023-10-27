package es.lavanda.filebot.executor.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotAMCException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.service.FilebotExecutorService;
import es.lavanda.filebot.executor.service.FilebotService;
import es.lavanda.lib.common.model.FilebotExecutionODTO;
import es.lavanda.lib.common.model.QbittorrentModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerService {

    private final FilebotService filebotServiceImpl;

    private final FilebotExecutorService filebotExecutorService;

    private static final String FILEBOT_TELEGRAM_RESOLUTION = "filebot-telegram-resolution";

    private static final String FILEBOT_NEW_EXECUTION_RESOLUTION = "filebot-new-execution-resolution";

    @RabbitListener(queues = FILEBOT_TELEGRAM_RESOLUTION)
    public void consumeMessageFilebotTelegramResolution(FilebotExecutionODTO filebotExecutionODTO) {
        log.info("Reading message of the queue {}: {}", FILEBOT_TELEGRAM_RESOLUTION, filebotExecutionODTO);
        filebotServiceImpl.resolutionTelegramBot(filebotExecutionODTO);
        log.info("Work message finished");
    }

    @RabbitListener(queues = FILEBOT_NEW_EXECUTION_RESOLUTION)
    public void consumeMessageFilebotNewExecutionResolution(QbittorrentModel qbittorrentModel) {
        log.info("Reading message of the queue {}: {}", FILEBOT_NEW_EXECUTION_RESOLUTION, qbittorrentModel);
        filebotExecutorService.resolutionQbittorrentModel(qbittorrentModel);
        log.info("Work message finished");
    }

}
