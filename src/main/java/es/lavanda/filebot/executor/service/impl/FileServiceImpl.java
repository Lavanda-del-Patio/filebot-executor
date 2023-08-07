package es.lavanda.filebot.executor.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

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
                // log.info("BASH commandline: {}", line);
                lsResult.add(line);
            });
            executorService.submit(streamGobbler);
            status = process.waitFor();
            if (status != 0) {
                log.error("LS command result on fail {}", lsResult.stream()
                        .map(n -> String.valueOf(n))
                        .collect(Collectors.joining("\n", "IN-", "-OUT")));
            } else {
                log.debug("LS command result on success {}", lsResult.stream()
                        .map(n -> String.valueOf(n))
                        .collect(Collectors.joining("\n", "IN-", "-OUT")));
            }
            return lsResult;
        } catch (InterruptedException | IOException e) {
            log.error("Exception on command 'LS'", e);
            Thread.currentThread().interrupt();
            throw new FilebotExecutorException("Exception on command 'LS'", e);
        }
    }

    @Override
    public void rmdir(String path) {
        int status;
        List<String> lsResult = new ArrayList<>();
        try {
            Process process = new ProcessBuilder("bash", "-c", "rmdir -r\"" + path + "\"").redirectErrorStream(true)
                    .start();
            StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), line -> {
                // log.info("BASH commandline: {}", line);
                lsResult.add(line);
            });
            executorService.submit(streamGobbler);
            status = process.waitFor();
            if (status != 0) {
                log.error("RMDIR command result on fail {}", lsResult.stream()
                        .map(n -> String.valueOf(n))
                        .collect(Collectors.joining("\n", "IN-", "-OUT")));
            } else {
                log.debug("RMDIR command result on success {}", lsResult.stream()
                        .map(n -> String.valueOf(n))
                        .collect(Collectors.joining("\n", "IN-", "-OUT")));
            }
        } catch (InterruptedException | IOException e) {
            log.error("Exception on command 'RMDIR'", e);
            Thread.currentThread().interrupt();
            throw new FilebotExecutorException("Exception on command 'RMDIR'", e);
        }
    }

}
