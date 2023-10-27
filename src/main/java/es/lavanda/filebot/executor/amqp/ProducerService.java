package es.lavanda.filebot.executor.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.lib.common.model.FilebotExecutionIDTO;
import es.lavanda.lib.common.model.QbittorrentModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerService {

    private final RabbitTemplate rabbitTemplate;

    private static final String FILEBOT_NEW_EXECUTION_QUEUE = "filebot-new-execution";
    private static final String FILEBOT_TELEGRAM = "filebot-telegram";

    public void createNewExecution(QbittorrentModel newExecution) {
        try {
            log.info("Sending message to queue {}", FILEBOT_NEW_EXECUTION_QUEUE);
            rabbitTemplate.convertAndSend(FILEBOT_NEW_EXECUTION_QUEUE, newExecution);
            log.info("Sended message to queue");
        } catch (Exception e) {
            log.error("Failed send message to queue {}", FILEBOT_NEW_EXECUTION_QUEUE, e);
            throw new FilebotExecutorException("Failed send message to queue", e);
        }
    }

    public void sendFilebotExecutionToTelegram(FilebotExecutionIDTO filebot) {
        try {
            log.info("Sending message to queue {}", FILEBOT_TELEGRAM);
            rabbitTemplate.convertAndSend(FILEBOT_TELEGRAM, filebot);
            log.info("Sended message to queue");
        } catch (Exception e) {
            log.error("Failed send message to queue {}", FILEBOT_TELEGRAM, e);
            throw new FilebotExecutorException("Failed send message to queue", e);
        }
    }
}
