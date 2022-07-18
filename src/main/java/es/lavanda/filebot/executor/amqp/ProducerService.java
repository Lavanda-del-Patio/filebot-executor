package es.lavanda.filebot.executor.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.lib.common.model.FilebotExecutionIDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void sendFilebotExecutionToTelegram(FilebotExecutionIDTO filebot) {
        try {
            log.info("Sending message to queue {}", "filebot-telegram");
            rabbitTemplate.convertAndSend("filebot-telegram", filebot);
            log.info("Sended message to queue");
        } catch (Exception e) {
            log.error("Failed send message to queue {}", "filebot-telegram", e);
            throw new FilebotExecutorException("Failed send message to queue", e);
        }
    }


    public void sendFilebotExecutionRecursive(FilebotExecution filebot) {
        try {
            log.info("Sending message to queue {}", "filebot-execution");
            rabbitTemplate.convertAndSend("filebot-execution", filebot);
            log.info("Sended message to queue");
        } catch (Exception e) {
            log.error("Failed send message to queue {}", "filebot-execution", e);
            throw new FilebotExecutorException("Failed send message to queue", e);
        }
    }


}
