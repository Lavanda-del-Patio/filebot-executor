package es.lavanda.filebot.executor.service.impl;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.amqp.ProducerService;
import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.QbittorrentModel;
import es.lavanda.filebot.executor.repository.FilebotExecutionRepository;
import es.lavanda.filebot.executor.service.FilebotExecutorService;
import es.lavanda.filebot.executor.service.FilebotService;
import es.lavanda.filebot.executor.util.FilebotUtils;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilebotExecutorServiceImpl implements FilebotExecutorService {

    @Autowired
    private FilebotExecutionRepository filebotExecutionRepository;

    @Autowired
    private FilebotService filebotService;

    @Autowired
    private FilebotUtils filebotUtils;

    @Autowired
    private ProducerService producerService;

    @Override
    public Page<FilebotExecution> getAllPageable(Pageable pageable) {
        return filebotExecutionRepository.findAllByOrderByLastModifiedAtDesc(pageable);
    }

    @Override
    public void createNewExecution(QbittorrentModel qbittorrentModel) {
        log.info("Creating new Filebot Execution named {}", qbittorrentModel.getName());
        if (filebotExecutionRepository.findByPath(qbittorrentModel.getPath().toString()).isPresent()) {
            throw new FilebotExecutorException("FilebotExecution already exists");
        }
        FilebotExecution filebotExecution = new FilebotExecution();
        filebotExecution.setPath(filebotUtils.getFilebotPathInput() + "/" + qbittorrentModel.getPath().toString());
        filebotExecution.setCategory(qbittorrentModel.getCategory());
        if (filebotExecution.getCategory().equalsIgnoreCase("tv-sonarr-en"))
            filebotExecution
                    .setCommand(filebotUtils.getFilebotCommand(Path.of(filebotExecution.getPath()), null, null, false, true));
        else {
            filebotExecution
                    .setCommand(filebotUtils.getFilebotCommand(Path.of(filebotExecution.getPath()), null, null, false, false));
        }
        filebotExecutionRepository.save(filebotExecution);
        try {
            producerService.sendFilebotExecutionRecursive(filebotExecution);
        } catch (Exception e) {
            log.error("Error with the execution {}", qbittorrentModel.getName(), e);
        }
    }
}
