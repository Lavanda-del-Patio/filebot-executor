package es.lavanda.filebot.executor.service.impl;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import es.lavanda.filebot.executor.amqp.ProducerService;
import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.QbittorrentModel;
import es.lavanda.filebot.executor.model.FilebotExecution.FilebotStatus;
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
    public FilebotExecution createNewExecution(QbittorrentModel qbittorrentModel) {
        log.info("Creating new Filebot Execution about torrent id {} and name", qbittorrentModel.getId(),
                qbittorrentModel.getName());
        if (filebotExecutionRepository.findByPath(qbittorrentModel.getName().toString()).isPresent()) {
            throw new FilebotExecutorException("FilebotExecution already exists");
        }
        FilebotExecution filebotExecution = new FilebotExecution();
        filebotExecution.setPath(filebotUtils.getFilebotPathInput() + "/" + qbittorrentModel.getName().toString());
        filebotExecution.setCategory(qbittorrentModel.getCategory());
        if (filebotExecution.getCategory().equalsIgnoreCase("tv-sonarr-en")) {
            filebotExecution.setEnglish(true);
        }
        filebotExecution
                .setCommand(filebotUtils.getFilebotCommand(Path.of(filebotExecution.getPath()), null, null, false,
                        filebotExecution.isEnglish()));
        filebotExecution = filebotExecutionRepository.save(filebotExecution);
        try {
            producerService.sendFilebotExecutionRecursive(filebotExecution);
        } catch (Exception e) {
            log.error("Error with the execution {}", qbittorrentModel.getName(), e);
        }
        return filebotExecution;
    }

    @Override
    public FilebotExecution reExecution(String id) {
        FilebotExecution filebotExecution = filebotExecutionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "FilebotExecution not found with the id " + id));
        filebotExecution.setStatus(FilebotStatus.UNPROCESSED);
        filebotExecution = filebotExecutionRepository.save(filebotExecution);
        try {
            producerService.sendFilebotExecutionRecursive(filebotExecution);
        } catch (Exception e) {
            log.error("Error with the execution {}", filebotExecution.getId(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error with the execution " + filebotExecution.getId());
        }
        return filebotExecution;
    }

    @Override
    public void delete(String id) {
        FilebotExecution filebotExecution = filebotExecutionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "FilebotExecution not found with the id " + id));
        filebotExecutionRepository.delete(filebotExecution);
    }
}
