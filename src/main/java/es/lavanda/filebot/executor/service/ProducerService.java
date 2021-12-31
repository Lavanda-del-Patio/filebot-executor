package es.lavanda.filebot.executor.service;

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

    public void sendFilebotExecution(FilebotExecutionIDTO filebot) {
        try {
            log.info("Sending message to queue {}", "filebot-executor");
            rabbitTemplate.convertAndSend("filebot-executor", filebot);
            log.info("Sended message to queue");
        } catch (Exception e) {
            log.error("Failed send message to queue {}", "filebot-executor", e);
            throw new FilebotExecutorException("Failed send message to queue", e);
        }
    }

}
