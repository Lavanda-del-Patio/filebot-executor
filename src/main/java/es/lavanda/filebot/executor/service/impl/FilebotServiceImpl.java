package es.lavanda.filebot.executor.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.amqp.ProducerService;
import es.lavanda.filebot.executor.exception.FilebotAMCException;
import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.FilebotExecution.FilebotStatus;
import es.lavanda.filebot.executor.repository.FilebotExecutionRepository;
import es.lavanda.filebot.executor.service.FilebotAMCExecutor;
import es.lavanda.filebot.executor.service.FilebotService;
import es.lavanda.filebot.executor.util.FilebotUtils;
import es.lavanda.lib.common.model.FilebotExecutionIDTO;
import es.lavanda.lib.common.model.FilebotExecutionODTO;
import es.lavanda.lib.common.service.NotificationService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilebotServiceImpl implements FilebotService {

    @Autowired
    private FilebotExecutionRepository filebotExecutionRepository;

    @Autowired
    private ExecutorService executorService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ProducerService producerService;

    @Autowired
    private FilebotAMCExecutor filebotAMCExecutor;

    @Autowired
    private FilebotUtils filebotUtils;

    private static final Pattern PATTERN_SELECT_CONTENT = Pattern.compile("Group:.*=> \\[(.*)\\]");

    private static final Pattern PATTERN_MOVED_CONTENT = Pattern.compile("\\[(.*)\\] from \\[(.*)\\] to \\[(.*)\\]");

    private static final Pattern PATTERN_FILE_EXIST = Pattern
            .compile("Skipped \\[(.*)\\] because \\[(.*)\\] already exists");

    @Override
    public FilebotExecution execute(FilebotExecution filebotExecution) {
        filebotExecution.setStatus(FilebotStatus.UNPROCESSED);
        save(filebotExecution);
        executionWithCommand(filebotExecution);
        return filebotExecution;
    }

    @Override
    public void resolution(FilebotExecutionODTO filebotExecutionODTO) {
        log.info("Resolution: {}", filebotExecutionODTO);
        Optional<FilebotExecution> optFilebotExecution = filebotExecutionRepository
                .findById(filebotExecutionODTO.getId());
        if (optFilebotExecution.isPresent()
                && Boolean.FALSE.equals(optFilebotExecution.get().getStatus() == FilebotStatus.PROCESSED)) {
            executionCompleteWithQuery(optFilebotExecution.get(),
                    Optional.ofNullable(filebotExecutionODTO.getQuery())
                            .orElse(filebotExecutionODTO.getSelectedPossibilitie()),
                    filebotExecutionODTO.getLabel(), filebotExecutionODTO.isForceStrict());
        } else {
            log.error("FilebotExecution not found: {}", filebotExecutionODTO);
        }
    }

    // @Override
    // public void execute() {
    // log.info("Executing Filebot Service");
    // List<Path> paths = getAllFilesFounded(filebotUtils.getFilebotPathInput());
    // paths.forEach(path -> {
    // // log.info(path.toString());
    // filebotExecutionRepository.findByFolderPath(path.toString()).ifPresentOrElse((filebotExecution)
    // -> {
    // if (filebotExecution.getStatus().equals(FilebotStatus.PROCESSED)) {
    // // log.info("Path {} PROCESSED, reexecution", path.toString());
    // // reexcutionWithCommand(filebotExecution);
    // } else if (filebotExecution.getStatus().equals(FilebotStatus.UNPROCESSED)) {
    // log.info("Path {} UNPROCESSED", path.toString());
    // executionComplete(filebotExecution);
    // } else if (filebotExecution.getStatus().equals(FilebotStatus.ERROR)
    // || filebotExecution.getStatus().equals(FilebotStatus.FILES_NOT_FOUND)) {
    // log.info("Path {} ERROR, reexecution", path.toString());
    // reexcutionWithCommand(filebotExecution);
    // }
    // }, () -> {
    // log.info("Path {} not processed. Starting new execution", path.toString());
    // // NO ESTA PRESENTE, SE TIENE QUE EJECUTAR
    // FilebotExecution filebotExecution = new FilebotExecution();
    // filebotExecution.setFolderPath(path.toString());
    // filebotExecution.setCommand(filebotUtils.getFilebotCommand(path, null, null,
    // false));
    // filebotExecution.setStatus(FilebotStatus.UNPROCESSED);
    // save(filebotExecution);
    // executionComplete(filebotExecution);
    // });
    // });
    // }

    private void executionWithCommand(FilebotExecution filebotExecution) {
        log.info("On ExecutionComplete");
        String execution = null;
        try {
            execution = filebotAMCExecutor
                    .execute(filebotExecution.getCommand());
            log.info("Result of Execution {}", execution);
            completedFilebotExecution(filebotExecution, execution);
        } catch (FilebotAMCException e) {
            handleException(filebotExecution, e.getExecutionMessage(), e);
        }
    }

    private void executionCompleteWithQuery(FilebotExecution filebotExecution, String query, String utLabel,
            boolean forceStrict) {
        log.info("On ExecutionComplete with Query");
        String execution = null;
        try {
            filebotExecution.setCommand(filebotUtils.getFilebotCommand(Path.of(filebotExecution.getPath()), query,
                    utLabel, forceStrict, filebotExecution.isEnglish()));
            save(filebotExecution);
            execution = filebotAMCExecutor
                    .execute(filebotExecution.getCommand());
            log.info("Result of Execution {}", execution);
            completedFilebotExecution(filebotExecution, execution);
        } catch (FilebotAMCException e) {
            handleException(filebotExecution, e.getExecutionMessage(), e);
        }
    }

    private void handleException(FilebotExecution filebotExecution, String execution, FilebotAMCException e) {
        switch (e.getType()) {
            case STRICT_QUERY:
                log.info("Handling STRICT_QUERY");
                strictOrQuery(filebotExecution, execution);
                break;
            case REGISTER:
                log.info("Handling REGISTER");
                filebotExecution.setStatus(FilebotStatus.ERROR);
                save(filebotExecution);
                tryLicensed();// Send notification;
                break;
            case SELECTED_OPTIONS:
                log.info("Handling SELECTED_OPTIONS");
                // selectOptions(filebotExecution, execution);
                break;
            case FILE_EXIST:
                log.info("File exist");
                fileExist(filebotExecution, execution);
                break;
            case FILES_NOT_FOUND:
                log.info("Files not found");
                filesNotFound(filebotExecution, execution);
                break;
            default:
                break;
        }
    }

    private void filesNotFound(FilebotExecution filebotExecution, String execution) {
        log.info("filesNotFound {}", execution);
        filebotExecution.setStatus(FilebotStatus.FILES_NOT_FOUND);
        save(filebotExecution);
    }

    private void fileExist(FilebotExecution filebotExecution, String execution) {
        log.info("FileExist {}", execution);
        Matcher matcherMovedContent = PATTERN_FILE_EXIST.matcher(execution);
        List<String> oldFilesName = new ArrayList<>();
        List<String> newFilesname = new ArrayList<>();
        while (matcherMovedContent.find()) {
            String fromContent = matcherMovedContent.group(1);
            String toContent = matcherMovedContent.group(2);
            log.info("From Content {}", fromContent);
            log.info("To Content {}", toContent);
            oldFilesName.add(getFilename(fromContent));
            newFilesname.add(getFilename(toContent));
        }
        // filebotExecution.setNewParentPath(getFolderPathOfFiles(newFilesname));
        filebotExecution.setFiles(oldFilesName);
        filebotExecution.setNewFiles(newFilesname);
        filebotExecution.setStatus(FilebotStatus.PROCESSED_EXISTED);
        save(filebotExecution);
    }

    private void tryLicensed() {
        log.info("Try to register license");
        try {
            // notificationService.sendNotification(SnsTopic.FILEBOT_LICENSE_ERROR,
            // "Filebot License Error",
            // "Filebot License Error");
            filebotAMCExecutor.execute(filebotUtils.getRegisterCommand());
            log.info("Licenced");
        } catch (FilebotAMCException e) {
            log.info("Not Licenced");
            throw e;
        }
    }

    private void selectOptions(FilebotExecution filebotExecution, String string) {
        // FilebotExecutionIDTO filebotExecutionIDTO = new FilebotExecutionIDTO();
        // filebotExecutionIDTO.setId(filebotExecution.getId());
        // filebotExecutionIDTO.setFiles(filebotExecution.getFilesName());
        // filebotExecutionIDTO.setPath(filebotExecution.getFolderPath());
        // filebotExecution.setPossibilities(Arrays.asList(string.split("\n")));
        // producerService.sendFilebotExecution(filebotExecutionIDTO);
    }

    private void strictOrQuery(FilebotExecution filebotExecution, String execution) {
        Matcher matcherGroupContent = PATTERN_SELECT_CONTENT.matcher(execution);
        List<String> groupContentList = new ArrayList<>();
        while (matcherGroupContent.find()) {
            String groupContent = matcherGroupContent.group(1);
            String[] groupContentSplit = groupContent.split(",");
            for (String splited : groupContentSplit) {
                groupContentList.add(splited);
            }
        }
        filebotExecution.setFiles(groupContentList);
        FilebotExecutionIDTO filebotExecutionIDTO = new FilebotExecutionIDTO();
        filebotExecutionIDTO.setId(filebotExecution.getId());
        filebotExecutionIDTO.setFiles(filebotExecution.getFiles());
        filebotExecutionIDTO.setPath(filebotExecution.getPath().toString());
        producerService.sendFilebotExecutionToTelegram(filebotExecutionIDTO);
        filebotExecution.setStatus(FilebotStatus.PROCESSING);
        save(filebotExecution);
    }

    private FilebotExecution save(FilebotExecution filebotExecution) {
        return filebotExecutionRepository.save(filebotExecution);
    }

    private List<Path> getAllFilesFounded(String path) {
        log.info("All files founded method");
        try (Stream<Path> walk = Files.walk(Paths.get(path), 1)) {
            List<Path> paths = walk.filter(Files::isDirectory)
                    .collect(Collectors.toList());
            paths.remove(0);
            return paths;
        } catch (IOException e) {
            log.error("Can not access to path {}", filebotUtils.getFilebotPathInput(), e);
            throw new FilebotExecutorException("Can not access to path", e);
        }
    }

    private void completedFilebotExecution(FilebotExecution filebotExecution, String execution) {
        log.info("CompletedFilebotExecution {}", execution);
        Matcher matcherMovedContent = PATTERN_MOVED_CONTENT.matcher(execution);
        List<String> oldFilesName = new ArrayList<>();
        List<String> newFilesname = new ArrayList<>();
        String newParentFolderPath = null;
        while (matcherMovedContent.find()) {
            String fromContent = matcherMovedContent.group(2);
            String toContent = matcherMovedContent.group(3);
            log.info("From Content {}", fromContent);
            log.info("To Content {}", toContent);
            oldFilesName.add(Path.of(fromContent).getFileName().toString());
            newFilesname.add(Path.of(toContent).getFileName().toString());
            newParentFolderPath = Path.of(toContent).getParent().getFileName().toString();
        }
        filebotExecution.setFiles(oldFilesName);
        filebotExecution.setNewFiles(newFilesname);
        filebotExecution.setStatus(FilebotStatus.PROCESSED);
        save(filebotExecution);
    }

    private String getFolderPathOfFiles(List<String> newFilesname) {
        for (String string : newFilesname) {
            Path path = Paths.get(string);
            return path.getParent().getFileName().toString();
        }
        return null;
    }

    private String getFolderPath(String folderPath) {
        if (Objects.nonNull(folderPath)) {
            Path path = Paths.get(folderPath);
            return path.getFileName().toString();
        }
        return null;
    }

    private String getFilename(String fromContent) {
        if (Objects.nonNull(fromContent)) {
            Path path = Paths.get(fromContent);
            return path.getFileName().toString();
        }
        return fromContent;
    }

}
