package es.lavanda.filebot.executor.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.service.FileService;
import es.lavanda.filebot.executor.util.StreamGobbler;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private ExecutorService executorService;

    @Override
    public List<String> ls(String path) {
        int status;
        List<String> lsResult = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("bash", "-c", "ls " + path).redirectErrorStream(true)
                    .start();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), line -> {
                log.info("BASH commandline: {}", line);
                lsResult.add(line);
            });
            executorService.submit(streamGobbler);
            status = process.waitFor();
            if (status != 0) {
                log.error("Todo mal");
            } else {
                log.info("Todo bien");
            }
            return lsResult;
        } catch (InterruptedException | IOException e) {
            log.error("Exception on command 'LS'", e);
            Thread.currentThread().interrupt();
            throw new FilebotExecutorException("Exception on command 'LS'", e);
        }
    }

}
