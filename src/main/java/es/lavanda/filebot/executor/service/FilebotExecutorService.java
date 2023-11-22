package es.lavanda.filebot.executor.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.QbittorrentModel;
import es.lavanda.lib.common.model.FilebotExecutionODTO;

public interface FilebotExecutorService {

    Page<FilebotExecution> getAllPageable(Pageable pageable, String status, String name);

    void delete(String id);

    FilebotExecution getById(String id);

    FilebotExecution editExecution(String id, FilebotExecution filebotExecution);

    List<String> getAllFiles();

    void checkPossiblesNewFilebotExecution();

      void resolutionTelegramBot(FilebotExecutionODTO filebotExecutionODTO);

      void createNewExecution(QbittorrentModel qbittorrentModel);
}
