package es.lavanda.filebot.executor.service.impl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotAMCException;
import es.lavanda.filebot.executor.exception.FilebotAMCException.Type;
import es.lavanda.filebot.executor.model.FilebotCommandExecution;
import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.service.FilebotAMCExecutor;
import es.lavanda.filebot.executor.util.StreamGobbler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilebotAMCExecutorImpl implements FilebotAMCExecutor {

    private final ExecutorService executorService;

    private static final Pattern PATTERN_PROCESSED_FILE = Pattern.compile("Processed \\d file");

    private static final Pattern PATTERN_FILES_NOT_FOUND = Pattern.compile("Exit status: 100");

    private static final Pattern PATTERN_NOT_FILE_SELECTED = Pattern.compile("No files selected for processing");

    private static final Pattern PATTERN_FILE_EXISTS = Pattern.compile("Skipped.*because.*already exists");

    @Override
    public FilebotCommandExecution execute(String command) {
        FilebotCommandExecution execution = filebotExecution(command);
        isNotLicensed(execution);
        isNonStrictOrQuery(execution);
        isNoFilesSelected(execution);
        isChooseOptions(execution);
        isFileExists(execution);
        isError(execution);
        return execution;
    }

    private FilebotCommandExecution filebotExecution(String command) {
        Process process = null;
        try {
            process = new ProcessBuilder("bash", "-c", command).redirectErrorStream(true)
                    .start();
            StringBuilder sbuilder = new StringBuilder();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), line -> {
                log.info("Filebot commandLine: {}", line);
                sbuilder.append(line);
                sbuilder.append("\n");
            });
            executorService.submit(streamGobbler);
            FilebotCommandExecution filebotCommandExecution = new FilebotCommandExecution();
            filebotCommandExecution.setExitStatus(process.waitFor());
            filebotCommandExecution.setLog(sbuilder.toString());
            log.info("Exit status: {}", filebotCommandExecution.getExitStatus());
            return filebotCommandExecution;
        } catch (InterruptedException | IOException e) {
            log.error("Exception on Filebot commandLine", e);
            Thread.currentThread().interrupt();
            throw new FilebotExecutorException("Exception on Filebot commandLine", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private void isNotLicensed(FilebotCommandExecution execution) {
        log.debug("Checking if is licensed");
        if (execution.getLog().contains("License Error: UNREGISTERED")) {
            throw new FilebotAMCException(Type.REGISTER, execution);
        }
    }

    private void isNoFilesSelected(FilebotCommandExecution execution) {
        Matcher matcherNoFilesSelected = PATTERN_NOT_FILE_SELECTED.matcher(execution.getLog());
        Matcher matcherFilesNotFound = PATTERN_FILES_NOT_FOUND.matcher(execution.getLog());
        if (execution.getExitStatus() == 100 || matcherNoFilesSelected.find() || matcherFilesNotFound.find()) {
            throw new FilebotAMCException(Type.FILES_NOT_FOUND, execution);
        }
    }

    private void isNonStrictOrQuery(FilebotCommandExecution execution) {
        if (execution.getLog().contains("Consider using -non-strict to enable opportunistic matching")
                || execution.getLog().contains("No episode data found:")
                || (notContainsProcessedFile(execution)
                        && execution.getLog().contains("Finished without processing any files"))) {
            throw new FilebotAMCException(Type.STRICT_QUERY, execution);
        }
    }

    private void isError(FilebotCommandExecution execution) {
        if (execution.getExitStatus() == 3) {
            throw new FilebotAMCException(Type.ERROR, execution);
        }
    }

    private boolean notContainsProcessedFile(FilebotCommandExecution execution) {
        Matcher matcherMovedContent = PATTERN_PROCESSED_FILE.matcher(execution.getLog());
        if (!matcherMovedContent.find()) {
            return true;
        }
        return false;
    }

    private void isFileExists(FilebotCommandExecution execution) {
        Matcher matcherMovedContent = PATTERN_FILE_EXISTS.matcher(execution.getLog());
        if (matcherMovedContent.find()) {
            throw new FilebotAMCException(Type.FILE_EXIST, execution);
        }
    }

    private void isChooseOptions(FilebotCommandExecution execution) {
        if (execution.getLog().contains("XXXX")) {
            throw new FilebotAMCException(Type.SELECTED_OPTIONS, execution);
        }
    }

}
