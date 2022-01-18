package es.lavanda.filebot.executor.service.impl;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.DirectoryStream.Filter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.amqp.ProducerService;
import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.FilebotExecution.FilebotStatus;
import es.lavanda.filebot.executor.repository.FilebotExecutionRepository;
import es.lavanda.filebot.executor.service.FilebotService;
import es.lavanda.filebot.executor.util.FilebotConstants;
import es.lavanda.filebot.executor.util.FilebotUtils;
import es.lavanda.filebot.executor.util.StreamGobbler;
import es.lavanda.lib.common.SnsTopic;
import es.lavanda.lib.common.model.FilebotExecutionIDTO;
import es.lavanda.lib.common.model.FilebotExecutionODTO;
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
    private ProducerService producerService;

    @Autowired
    private FilebotUtils filebotUtils;

    private static final Pattern PATTERN_SELECT_CONTENT = Pattern.compile("Group:.*\\[(.*)\\]");

    @Override
    public void execute() {
        log.info("Executing Filebot Service");
        List<Path> paths = getAllFilesFounded(filebotUtils.getFilebotPathInput());
        paths.forEach(path -> {
            filebotExecutionRepository.findByFolderPath(path.toString()).ifPresentOrElse((filebotExecution)->{
                if (filebotExecution.getStatus().equals(FilebotStatus.PROCESSED)){

                }
            },()-> {



            });
            });

            if (Boolean.FALSE.equals(filebotExecutionRepository.existsByFolderPath(path.toString()))) {
                log.info(path.toString());
                FilebotExecution filebotExecution = new FilebotExecution();
                filebotExecution.setFolderPath(path.toString());
                filebotExecution.setCommand(filebotUtils.getFilebotCommand(path));
                filebotExecution.setStatus(FilebotStatus.UNPROCESSED);
                save(filebotExecution);
                StringBuilder execution = filebotExecution(filebotExecution);
                filebotExecution = save(filebotExecution);
                log.info("Execution: {}", execution);
                if (isNotLicensed(execution.toString())) {
                    log.info("Is not licensed");
                    tryRegistered();
                } else if (needsNonStrictOrQuery(execution.toString())) {
                    log.info("Needs non-strict or query");
                    strictOrQuery(filebotExecution, execution.toString());
                    filebotExecution.setStatus(FilebotStatus.PROCESSING);
                    save(filebotExecution);
                } else if (isChooseOptions(execution.toString())) {
                    log.info("Needs select options");
                    selectOptions(filebotExecution, execution.toString());
                    filebotExecution.setStatus(FilebotStatus.PROCESSING);
                    save(filebotExecution);
                } else {
                    log.info("Moved files. All correct");
                    // fillFilebotExecution(filebotExecution, execution.toString());
                    filebotExecution.setStatus(FilebotStatus.PROCESSED);
                    save(filebotExecution);
                }
            } else {
                log.debug("Adding files to the filebotExecution");
                FilebotExecution filebotExecution = filebotExecutionRepository.findByFolderPath(path.toString());
                if (filebotExecution.getStatus().equals(FilebotStatus.UNPROCESSED)) {

                }
            }

        });
        log.info("Finished execution Filebot Service");
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
        if (matcherGroupContent.find()) {
            String groupContent = matcherGroupContent.group(1);
            String[] groupContentSplit = groupContent.split(",");
            List<String> groupContentList = new ArrayList<>();
            for (String splited : groupContentSplit) {
                groupContentList.add(splited);
            }
            filebotExecution.setFilesName(groupContentList);
        }
        FilebotExecutionIDTO filebotExecutionIDTO = new FilebotExecutionIDTO();
        filebotExecutionIDTO.setId(filebotExecution.getId());
        filebotExecutionIDTO.setFiles(filebotExecution.getFilesName());
        filebotExecutionIDTO.setPath(filebotExecution.getFolderPath());
        producerService.sendFilebotExecution(filebotExecutionIDTO);
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

    @Override
    public void resolution(FilebotExecutionODTO filebotExecutionODTO) {
        log.info("REsolution: {}", filebotExecutionODTO);
        // TODO Auto-generated method stub

    }
}
