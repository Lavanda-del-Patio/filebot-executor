package es.lavanda.filebot.executor.service.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import es.lavanda.filebot.executor.amqp.ProducerService;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.FilebotExecution.FileExecutor;
import es.lavanda.filebot.executor.model.FilebotExecution.FilebotStatus;
import es.lavanda.filebot.executor.model.QbittorrentModel;
import es.lavanda.filebot.executor.repository.FilebotExecutionRepository;
import es.lavanda.filebot.executor.service.FileService;
import es.lavanda.filebot.executor.service.FilebotExecutorService;
import es.lavanda.filebot.executor.util.FilebotUtils;
import es.lavanda.lib.common.model.FilebotExecutionIDTO;
import es.lavanda.lib.common.model.FilebotExecutionODTO;
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

  @Autowired
  private FileService fileService;

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
  public void resolutionTelegramBot(FilebotExecutionODTO filebotExecutionODTO) {
    Optional<FilebotExecution> optFilebotExecution = filebotExecutionRepository
        .findById(filebotExecutionODTO.getId());
    if (optFilebotExecution.isPresent()
        && Boolean.FALSE.equals(optFilebotExecution.get().getStatus() == FilebotStatus.PROCESSED)) {
      FilebotExecution filebotExecution = optFilebotExecution.get();
      filebotExecution.setCategory(filebotExecutionODTO.getCategory());
      filebotExecution.setAction(filebotExecutionODTO.getAction());
      filebotExecution.setForceStrict(filebotExecutionODTO.isForceStrict());
      if (filebotExecution.getCategory().equals(FilebotCategory.TV_EN)) {
        filebotExecution.setEnglish(true);
      }
      filebotExecution
          .setCommand(filebotUtils.getFilebotCommand(Path.of(filebotExecution.getPath()),
              filebotExecutionODTO.getQuery(), filebotExecution.getCategory(), filebotExecution.isForceStrict(),
              filebotExecution.isEnglish(), filebotExecution.getAction()));
      filebotExecution.setStatus(FilebotStatus.PENDING);
      filebotExecutionRepository.save(filebotExecution);
      log.info("FilebotExecution founded and updated: {}", filebotExecutionODTO);

    } else {
      log.error("FilebotExecution not found: {}", filebotExecutionODTO);
    }
  }

  @Override
  public void checkPossiblesNewFilebotExecution() {
    List<String> files = getAllFilesInput();
    for (String file : files) {
      if (filebotExecutionRepository.findByName(file).isPresent()) {
        log.info("Already exists {}", file);
      } else {
        if (fileServiceImpl.isValidForFilebot(filebotUtils.getFilebotPathInput() + "/" + file)) {
          log.info("Creating new Execution {}", file);
          FilebotExecution filebotExecution = new FilebotExecution();
          filebotExecution.setPath(filebotUtils.getFilebotPathInput() + "/" + file);
          filebotExecution.setName(file);
          if (fileServiceImpl.isDirectory(filebotUtils.getFilebotPathInput() + "/" + file)) {
            filebotExecution.setFiles(fileService.getFilesExecutor(filebotExecution.getPath()));
          } else {
            FileExecutor fileExecutor = new FileExecutor();
            fileExecutor.setFile(filebotUtils.getFilebotPathInput() + "/" + file);
            filebotExecution.setFiles(List.of(fileExecutor));
          }
          filebotExecution.setStatus(FilebotStatus.ON_TELEGRAM);
          if (filebotExecution.getFiles().size() > 0) {
            filebotExecution = filebotExecutionRepository.save(filebotExecution);
            FilebotExecutionIDTO filebotExecutionIDTO = new FilebotExecutionIDTO();
            filebotExecutionIDTO.setId(filebotExecution.getId());
            filebotExecutionIDTO.setPath(filebotExecution.getPath().toString());
            filebotExecutionIDTO.setFiles(fileService.ls(filebotExecution.getPath()));
            filebotExecutionIDTO.setName(filebotExecution.getName());
            producerService.sendFilebotExecutionToTelegram(filebotExecutionIDTO);
          } else {
            log.info("Path not valid, not contain valid files or is empty{}", file);
            continue;
          }
        } else {
          log.info("File not valid {}", file);
        }

      }
    }
  }

  @Override
  public void createNewExecution(QbittorrentModel qbittorrentModel) {
    log.info("Creating new Execution {}", qbittorrentModel.getName());
    FilebotExecution filebotExecution = new FilebotExecution();
    filebotExecution.setPath(filebotUtils.getFilebotPathInput() + "/" + qbittorrentModel.getName());
    filebotExecution.setName(qbittorrentModel.getName());
    filebotExecution.setStatus(FilebotStatus.ON_TELEGRAM);
    filebotExecution = filebotExecutionRepository.save(filebotExecution);

    FilebotExecutionIDTO filebotExecutionIDTO = new FilebotExecutionIDTO();
    filebotExecutionIDTO.setId(filebotExecution.getId());
    filebotExecutionIDTO.setPath(filebotExecution.getPath().toString());
    filebotExecutionIDTO.setFiles(fileService.ls(filebotExecution.getPath()));
    producerService.sendFilebotExecutionToTelegram(filebotExecutionIDTO);
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

}
