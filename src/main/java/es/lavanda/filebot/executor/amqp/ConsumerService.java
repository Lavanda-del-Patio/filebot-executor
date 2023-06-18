package es.lavanda.filebot.executor.amqp;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotAMCException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.service.FilebotService;
import es.lavanda.lib.common.model.FilebotExecutionODTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsumerService {

    private final FilebotService filebotServiceImpl;


    @RabbitListener(queues = "filebot-telegram-resolution")
    public void consumeMessageFilebotTelegramResolution(FilebotExecutionODTO filebotExecutionODTO) {
        log.info("Reading message of the queue filebot-telegram-resolution: {}", filebotExecutionODTO);
        filebotServiceImpl.resolution(filebotExecutionODTO);
        log.info("Work message finished");
    }

    // @RabbitListener(queues = "filebot-executor")
    // public void consumeMessageFilebotExecutor(FilebotExecution filebotExecution) {
    //     log.info("Reading message of the queue filebot-executor: {}", filebotExecution);
    //    (filebotExecution);
    //     log.info("Work message finished");
    // }

    // @RabbitListener(queues = "filebot-execution")
    // public void consumeMessage(FilebotExecution filebotExecution) {
    // log.info("Reading message of the queue filebot-execution: {}",
    // filebotExecution);
    // filebotServiceImpl.execute(filebotExecution);
    // log.info("Work message finished");
    // }

    // @RabbitListener(queues = "bittorent-checker")
    // public void consumeMessageFromBittorrentChecker(FilebotExecutionODTO
    // filebotExecutionODTO) {
    // log.debug("Reading message of the queue bittorent-checker: {}",
    // filebotExecutionODTO);
    // // filebotServiceImpl.resolution(filebotExecutionODTO);
    // log.debug("Work message finished");
    // }

}
