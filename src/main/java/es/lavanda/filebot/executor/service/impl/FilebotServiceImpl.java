package es.lavanda.filebot.executor.service.impl;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream.Filter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.FilebotExecution.FilebotStatus;
import es.lavanda.filebot.executor.repository.FilebotExecutionRepository;
import es.lavanda.filebot.executor.service.FilebotService;
import es.lavanda.filebot.executor.util.FilebotConstants;
import es.lavanda.filebot.executor.util.FilebotUtils;
import es.lavanda.filebot.executor.util.StreamGobbler;
import es.lavanda.lib.common.SnsTopic;
import es.lavanda.lib.common.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import java.io.File;

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
    private FilebotUtils filebotUtils;

    @Override
    public void execute() {
        List<Path> paths = getAllFilesFounded(filebotUtils.getFilebotPathInput());
        paths.forEach(path -> {
            log.info(path.toString());
            FilebotExecution filebotExecution = new FilebotExecution();
            filebotExecution.setFolderPath(path);
            filebotExecution.setCommand(filebotUtils.getFilebotCommand(path));
            filebotExecution.setStatus(FilebotStatus.UNPROCESSED);
            save(filebotExecution);
            String execution = filebotExecution(filebotExecution);
            filebotExecution.setStatus(FilebotStatus.PROCESSING);
            save(filebotExecution);
            if (isNotLicensed(execution)) {
                log.info("Is not licensed");
                tryRegistered();
            } else if (needsNonStrictOrQuery(execution)) {
                log.info("Needs non-strict or query");
                // producerService.sendFilebotExecution(filebotExecution);
            } else if (isChooseOptions(execution)) {
                log.info("Needs select options");
                // producerService.sendFilebotExecution(filebotExecution);
            } else {
                log.info("Moved files. All correct");
            }
        });
    }

    private boolean isChooseOptions(String execution) {
        return false;
    }

    private boolean needsNonStrictOrQuery(String execution) {
        if (execution.contains("Consider using -non-strict to enable opportunistic matching")) {
            return true;
        }
        return false;
    }

    private String filebotExecution(FilebotExecution filebot) throws FilebotExecutorException {
        try {
            Process process = new ProcessBuilder("bash", "-c", filebot.getCommand()).redirectErrorStream(true)
                    .start();
            StringBuilder sbuilder = new StringBuilder();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), line -> {
                log.debug("Filebot commandline: {}", line);
                sbuilder.append(line);
                sbuilder.append(System.getProperty("line.separator"));
            });
            executorService.submit(streamGobbler);
            int status = process.waitFor();
            if (status != 0) {
                log.error("Todo mal");
            } else {
                log.error("Todo bien");
            }
            return sbuilder.toString();
        } catch (InterruptedException | IOException e) {
            log.error("Exception on command line transcode", e);
            Thread.currentThread().interrupt();
            throw new FilebotExecutorException("Exception on command line transcode", e);
        }
    }

    private String tryRegistered() {
        log.info("Try register filebot");
        try {
            Process process = new ProcessBuilder("bash", "-c",
                    "filebot --license " + filebotUtils.getFilebotPathData() + " license.psm")
                            .redirectErrorStream(true)
                            .start();
            StringBuilder sbuilder = new StringBuilder();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), line -> {
                log.debug("Filebot commandline: {}", line);
                sbuilder.append(line);
            });
            executorService.submit(streamGobbler);
            int status = process.waitFor();
            if (status != 0) {
                log.error("Todo mal");
            } else {
                log.error("Todo bien");
            }
            return sbuilder.toString();
        } catch (InterruptedException | IOException e) {
            log.error("Exception on command line transcode", e);
            Thread.currentThread().interrupt();
            throw new FilebotExecutorException("Exception on command line transcode", e);
        }
    }

    private boolean isNotLicensed(String lines) {
        log.info("Checking if is licensed");
        if (lines.contains("License Error: UNREGISTERED")) {
            // notificationService.send(SnsTopic.TELEGRAM_MESSAGE, "Filebot not registered.
            // Please fix it", "filebot-execution");
            return true;
        }
        return false;
    }
    private FilebotExecution save(FilebotExecution filebotExecution) {
        return filebotExecutionRepository.save(filebotExecution);
    }

    private List<Path> getAllFilesFounded(String path) {
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {
            List<Path> paths = walk.filter(Files::isDirectory)
                    .collect(Collectors.toList());
            paths.remove(0);
            return paths;
        } catch (IOException e) {
            log.error("Can not access to path {}", filebotUtils.getFilebotPathInput(), e);
            throw new FilebotExecutorException("Can not access to path", e);
        }
    }
}
