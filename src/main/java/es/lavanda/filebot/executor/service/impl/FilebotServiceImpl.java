package es.lavanda.filebot.executor.service.impl;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.exception.FilebotParserException;
import es.lavanda.filebot.executor.model.Filebot;
import es.lavanda.filebot.executor.model.FilebotFile;
import es.lavanda.filebot.executor.repository.FilebotFileRepository;
import es.lavanda.filebot.executor.repository.FilebotRepository;
import es.lavanda.filebot.executor.service.FilebotService;
import es.lavanda.filebot.executor.util.FilebotParser;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilebotServiceImpl implements FilebotService {
    
    @Autowired
    private FilebotRepository filebotRepository;

    @Autowired
    private FilebotFileRepository filebotFileRepository;

    @Autowired
    private FilebotParser filebotParser;

    @Value("${filebot.path}")
    private String FILEBOT_PATH;


    private String getHtmlData(String filePath) {
        try {
            return Files.readString(Path.of(filePath));
        } catch (IOException e) {
            log.error("Can not access to path {}", FILEBOT_PATH, e);
            throw new FilebotParserException("Can not access to path", e);
        }
    }

    private List<FilebotFile> getAllFilesFounded(String path) {
        try (Stream<Path> walk = Files.walk(Paths.get(path))) {

            return walk.filter(Files::isRegularFile).map(filePath -> new FilebotFile(filePath.toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Can not access to path {}", FILEBOT_PATH, e);
            throw new FilebotParserException("Can not access to path", e);
        }
    }


    // @Override
    // public void run(String... args) throws Exception {
    //     log.info("Start schedule parse new files");
    //     List<FilebotFile> newFiles = getAllFilesFounded(FILEBOT_PATH);
    //     List<FilebotFile> oldFiles = (List<FilebotFile>) filebotFileRepository.findAll();
    //     newFiles.removeAll(oldFiles);
    //     newFiles.forEach(file -> {
    //         log.info("Parsing new file {}", file.getFilePath());
    //         List<Filebot> filebots = filebotParser.parseHtml(getHtmlData(file.getFilePath()));
    //         filebotRepository.saveAll(filebots);
    //         filebotFileRepository.save(file);
    //     });
    //     log.info("Finish schedule parse new files");        
    // }



    // public int transcode(TranscodeMediaDTO transcodeMediaDTO) throws TranscoderException {
    //     log.info("Transcoding file {}", transcodeMediaDTO.toString());
    //     transcodeMediaDTO.setActive(true);
    //     producerService.sendStatus(transcodeMediaDTO);
    //     try {
    //         finalTime = 0;
    //         Process process = new ProcessBuilder("bash", "-c", transcodeMediaDTO.getCommand()).redirectErrorStream(true)
    //                 .start();
    //         StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), line -> {
    //             log.debug("FFMPEG commandline: {}", line);
    //             Matcher progressMatcher = progreesVideoPattern.matcher(line);
    //             Matcher durationVideoMatcher = durationVideoPattern.matcher(line);
    //             if (progressMatcher.find()) {
    //                 double diference = getDifference(finalTime, progressMatcher.group(0));
    //                  transcodeMediaDTO.setProcessed(diference);
    //                 if ((Math.round(transcodeMediaDTO.getProcessed() * 100.0) / 100.0) != 100) {
    //                     try {
    //                         producerService.sendStatus(transcodeMediaDTO);
    //                     } catch (TranscoderException e) {
    //                         log.error("Runtime transcoderException", e);
    //                         throw new TranscoderRuntimeException("Runtime transcoderException", e);
    //                     }
    //                 }
    //             }
    //             if (durationVideoMatcher.find()) {
    //                 log.debug("durationVideoMatcher found: {}", durationVideoMatcher.group(0));
    //                 finalTime = getDuration(durationVideoMatcher.group(0));
    //             }
    //         });
    //         executorService.submit(streamGobbler);
    //         int status = process.waitFor();
    //         if (status != 0) {
    //             transcodeMediaDTO.setProcessed(0);
    //             transcodeMediaDTO.setError(true);
    //         } else {
    //             transcodeMediaDTO.setProcessed(100);
    //         }
    //         transcodeMediaDTO.setActive(false);
    //         producerService.sendStatus(transcodeMediaDTO);
    //         log.info("File {} transcode with status {}", transcodeMediaDTO.toString(), status);
    //         return status;
    //     } catch (InterruptedException | IOException e) {
    //         log.error("Exception on command line transcode", e);
    //         transcodeMediaDTO.setProcessed(0);
    //         transcodeMediaDTO.setError(true);
    //         transcodeMediaDTO.setActive(false);
    //         producerService.sendStatus(transcodeMediaDTO);
    //         Thread.currentThread().interrupt();
    //         throw new TranscoderException("Exception on command line transcode", e);
    //     }
    // }
}
