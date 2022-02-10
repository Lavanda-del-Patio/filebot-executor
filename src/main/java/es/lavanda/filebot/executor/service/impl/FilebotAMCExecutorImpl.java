package es.lavanda.filebot.executor.service.impl;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotAMCException;
import es.lavanda.filebot.executor.exception.FilebotAMCException.Type;
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

    @Override
    public String execute(String command) {
        StringBuilder execution = filebotExecution(command);
        isNotLicensed(execution.toString());
        isNonStrictOrQuery(execution.toString());
        isChooseOptions(execution.toString());
        return execution.toString();
    }

    private StringBuilder filebotExecution(String command) {
        int status;
        try {
            Process process = new ProcessBuilder("bash", "-c", command).redirectErrorStream(true)
                    .start();
            StringBuilder sbuilder = new StringBuilder();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), line -> {
                log.info("Filebot commandline: {}", line);
                sbuilder.append(line);
                sbuilder.append("\n");
            });
            executorService.submit(streamGobbler);
            status = process.waitFor();
            if (status != 0) {
                log.error("Todo mal");
            } else {
                log.error("Todo bien");
            }
            return sbuilder;
        } catch (InterruptedException | IOException e) {
            log.error("Exception on command line transcode", e);
            Thread.currentThread().interrupt();
            throw new FilebotExecutorException("Exception on command line transcode", e);
        }
    }

    private void isNotLicensed(String execution) {
        log.debug("Checking if is licensed");
        if (execution.contains("License Error: UNREGISTERED")) {
            throw new FilebotAMCException(Type.REGISTER, execution);
        }
    }

    private void isNonStrictOrQuery(String execution) {
        if (execution.contains("Consider using -non-strict to enable opportunistic matching")
                || execution.contains("No episode data found:")
                || (notContainsProcessedFile(execution)
                        && execution.contains("Finished without processing any files"))) {
            throw new FilebotAMCException(Type.STRICT_QUERY, execution);
        }
    }

    private boolean notContainsProcessedFile(String execution) {
        Matcher matcherMovedContent = PATTERN_PROCESSED_FILE.matcher(execution);
        if (!matcherMovedContent.find()) {
            return true;
        }
        return false;
    }

    private void isChooseOptions(String execution) {
        if (execution.contains("XXXX")) {
            throw new FilebotAMCException(Type.SELECTED_OPTIONS, execution);
        }
    }

}
