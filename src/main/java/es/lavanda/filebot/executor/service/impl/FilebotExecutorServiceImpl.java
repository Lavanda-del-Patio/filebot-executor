package es.lavanda.filebot.executor.service.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import es.lavanda.filebot.executor.amqp.ProducerService;
import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.FilebotExecution.FilebotStatus;
import es.lavanda.filebot.executor.repository.FilebotExecutionRepository;
import es.lavanda.filebot.executor.service.FileService;
import es.lavanda.filebot.executor.service.FilebotExecutorService;
import es.lavanda.filebot.executor.util.FilebotUtils;
import es.lavanda.lib.common.model.QbittorrentModel;
import es.lavanda.lib.common.model.filebot.FilebotCategory;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilebotExecutorServiceImpl implements FilebotExecutorService {

  @Autowired
  private FilebotExecutionRepository filebotExecutionRepository;

  @Autowired
  private FilebotUtils filebotUtils;

  @Autowired
  private FileService fileServiceImpl;

  @Autowired
  private ProducerService producerService;

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
    log.debug("findAllByOrderByLastModifiedAtDesc");

    return filebotExecutionRepository.findAllByOrderByLastModifiedAtDesc(pageable);
  }

  @Override
  public FilebotExecution createNewExecution(QbittorrentModel qbittorrentModel) {
    log.info("Going to create new Filebot Execution about torrent id {} and name {}", qbittorrentModel.getId(),
        qbittorrentModel.getName());
    if (filebotExecutionRepository.findByName(qbittorrentModel.getName()).isPresent()) {
      log.info("Already exists {} // {}", qbittorrentModel.getId(),
          qbittorrentModel.getName());
      throw new FilebotExecutorException("FilebotExecution already exists");
    } else {
      log.info("Creating... {}", qbittorrentModel.getId(),
          qbittorrentModel.getName());
      FilebotExecution filebotExecution = new FilebotExecution();
      filebotExecution.setPath(filebotUtils.getFilebotPathInput() + "/" + qbittorrentModel.getName().toString());
      filebotExecution.setName(qbittorrentModel.getName());
      filebotExecution.setCategory(qbittorrentModel.getCategory());
      filebotExecution.setAction(qbittorrentModel.getAction());
      if (filebotExecution.getCategory().equals(FilebotCategory.TV_EN)) {
        filebotExecution.setEnglish(true);
      }
      filebotExecution
          .setCommand(filebotUtils.getFilebotCommand(Path.of(filebotExecution.getPath()), null, null, true,
              filebotExecution.isEnglish(), filebotExecution.getAction()));
      return filebotExecutionRepository.save(filebotExecution);
    }
  }

  @Override
  public void checkPossiblesNewFilebotExecution() {
    List<String> files = getAllFilesInput();
    for (String file : files) {
      if (filebotExecutionRepository.findByName(file).isPresent()) {
        log.info("Already exists {}", file);
      } else {
        log.info("Asking for new execution {}", file);
        QbittorrentModel qbittorrentModel = new QbittorrentModel();
        qbittorrentModel.setName(file);
        producerService.createNewExecution(qbittorrentModel);
      }
    }
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
      if (Boolean.FALSE.equals(fe.isManual())) {
        fe.setPath(filebotUtils.getFilebotPathInput() + "/" + filebotExecution.getPath());
      }
      fe.setCategory(filebotExecution.getCategory());
      if (filebotExecution.getCategory().equals(FilebotCategory.TV_EN)) {
        fe.setEnglish(true);
      }
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
    return getAllFilesInput();
  }

  private List<String> getAllFilesInput() {
    return fileServiceImpl.ls(filebotUtils.getFilebotPathInput());
  }

  @Override
  public void resolutionQbittorrentModel(QbittorrentModel qbittorrentModel) {
    createNewExecution(qbittorrentModel);
  }
}
