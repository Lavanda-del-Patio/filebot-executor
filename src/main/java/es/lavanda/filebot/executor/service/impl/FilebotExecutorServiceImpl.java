package es.lavanda.filebot.executor.service.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
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
import es.lavanda.filebot.executor.model.FilebotExecution.FilebotAction;
import es.lavanda.filebot.executor.model.FilebotExecution.FilebotStatus;
import es.lavanda.filebot.executor.repository.FilebotExecutionRepository;
import es.lavanda.filebot.executor.service.FileService;
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
  private FileService fileServiceImpl;

  @Override
  public Page<FilebotExecution> getAllPageable(Pageable pageable, String status, String path) {
    if (Objects.nonNull(status) && Objects.nonNull(path)) {
      // log.info("findAllByStatusAndPath");
      return filebotExecutionRepository.findAllByStatusAndPathContainingIgnoreCaseOrderByLastModifiedAtDesc(pageable,
          status, path);
    } else if (Objects.nonNull(path)) {
      // log.info("findAllByPath");
      return filebotExecutionRepository.findAllByPathIgnoreCaseContainingOrderByLastModifiedAtDesc(pageable, path);
    } else if (Objects.nonNull(status)) {
      // log.info("findAllByStatus");
      return filebotExecutionRepository.findAllByStatusOrderByLastModifiedAtDesc(pageable, status);
    }
    log.info("findAllByOrderByLastModifiedAtDesc");

    return filebotExecutionRepository.findAllByOrderByLastModifiedAtDesc(pageable);
  }

  @Override
  public FilebotExecution createNewExecution(QbittorrentModel qbittorrentModel) {
    log.info("Creating new Filebot Execution about torrent id {} and name {}", qbittorrentModel.getId(),
        qbittorrentModel.getName());
    if (filebotExecutionRepository.findByPath(qbittorrentModel.getName().toString()).isPresent()) {
      throw new FilebotExecutorException("FilebotExecution already exists");
    }
    FilebotExecution filebotExecution = new FilebotExecution();
    filebotExecution.setPath(filebotUtils.getFilebotPathInput() + "/" + qbittorrentModel.getName().toString());
    filebotExecution.setCategory(qbittorrentModel.getCategory());
    filebotExecution.setAction(FilebotAction.COPY);
    if (filebotExecution.getCategory().equalsIgnoreCase("tv-sonarr-en")) {
      filebotExecution.setEnglish(true);
    }
    filebotExecution
        .setCommand(filebotUtils.getFilebotCommand(Path.of(filebotExecution.getPath()), null, null, false,
            filebotExecution.isEnglish(), filebotExecution.getAction()));
    return filebotExecutionRepository.save(filebotExecution);
  }

  @Override
  public void delete(String id) {
    FilebotExecution filebotExecution = filebotExecutionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "FilebotExecution not found with the id " + id));
    filebotExecutionRepository.delete(filebotExecution);
  }

  @Override
  public FilebotExecution getById(String id) {
    return filebotExecutionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
            "FilebotExecution not found with the id " + id));
  }

  @Override
  public FilebotExecution editExecution(String id, FilebotExecution filebotExecution) {
    checkSameId(filebotExecution, id);
    FilebotExecution filebotExecutionToEdit = filebotExecutionRepository.findById(id).map(fe -> {
      fe.setPath(filebotUtils.getFilebotPathInput() + "/" + filebotExecution.getPath());
      fe.setCategory(filebotExecution.getCategory());
      fe.setEnglish(fe.getCategory().equalsIgnoreCase("tv-sonarr-en") ? true : false);
      fe.setAction(filebotExecution.getAction());
      fe.setCommand(Objects.nonNull(filebotExecution.getCommand()) ? filebotExecution.getCommand()
          : filebotUtils.getFilebotCommand(Path.of(fe.getPath()), null,
              null, false, fe.isEnglish(), filebotExecution.getAction()));
      fe.setStatus(FilebotStatus.UNPROCESSED);
      return fe;
    }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
        "FilebotExecution not found with the id " + id));
    return filebotExecutionRepository.save(filebotExecutionToEdit);
  }

  private void checkSameId(FilebotExecution filebotExecution, String id) {
    if (!filebotExecution.getId().equals(id)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "The id of the filebotExecution to edit is not the same as the id of the filebotExecution to edit");
    }
  }

  @Override
  public List<String> getAllFiles() {
    return getAllFiles(null);
  }

  @Override
  public void createBatchExecutionForMovie() {
    // for (String folderMovie : getAllFiles("/")) {
    List<String> allFiles = getAllFiles("/PeliculasToOrdered");
    int min = 0;
    int max = allFiles.size();
    int selectedFolder = (int) (Math.random() * (max - min + 1) + min);
    createNewExecution(allFiles.get(selectedFolder), "radarr", "/PeliculasToOrdered");
    // }
  }

  @Override
  public void createBatchExecutionForShow() {
    // for (String folderMovie : getAllFiles("/")) {
    List<String> allFiles = getAllFiles("/SeriesToOrdered");
    int min = 0;
    int max = allFiles.size();
    int selectedFolder = (int) (Math.random() * (max - min + 1) + min);
    createNewExecution(allFiles.get(selectedFolder), "tv-sonarr", "/SeriesToOrdered");
    // }
  }

  private List<String> getAllFiles(String path) {
    if (Objects.isNull(path)) {
      fileServiceImpl.ls(filebotUtils.getFilebotPathInput());
    }
    return fileServiceImpl.ls(filebotUtils.getFilebotPathOutput() + path);
  }

  private FilebotExecution createNewExecution(String path, String category, String folderPathForInput) {
    log.info("Creating new manual Filebot Execution about torrent path {} name",
        path);
    if (filebotExecutionRepository.findByPath(path).isPresent()) {
      throw new FilebotExecutorException("FilebotExecution already exists");
    }
    FilebotExecution filebotExecution = new FilebotExecution();
    filebotExecution.setPath(filebotUtils.getFilebotPathOutput() + folderPathForInput + "/" + path);
    filebotExecution.setCategory(category);
    filebotExecution.setAction(FilebotAction.MOVE);
    if (filebotExecution.getCategory().equalsIgnoreCase("tv-sonarr-en")) {
      filebotExecution.setEnglish(true);
    }
    filebotExecution
        .setCommand(filebotUtils.getFilebotCommand(Path.of(filebotExecution.getPath()), null, null, false,
            filebotExecution.isEnglish(), filebotExecution.getAction()));
    return filebotExecutionRepository.save(filebotExecution);
  }

}
