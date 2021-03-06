package es.lavanda.filebot.executor.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.QbittorrentModel;

public interface FilebotExecutorService {

    Page<FilebotExecution> getAllPageable(Pageable pageable);

    void createNewExecution(QbittorrentModel qbittorrentModel);

}
