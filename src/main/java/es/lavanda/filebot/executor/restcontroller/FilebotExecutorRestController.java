package es.lavanda.filebot.executor.restcontroller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.QbittorrentModel;
import es.lavanda.filebot.executor.service.FilebotExecutorService;
import es.lavanda.filebot.executor.service.FilebotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filebot-executor")
@Slf4j
@CrossOrigin(allowedHeaders = "*", origins = { "http://localhost:4200", "https://lavandadelpatio.es",
        "https://pre.lavandadelpatio.es" }, allowCredentials = "true", exposedHeaders = "*", methods = {
                RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.PATCH, RequestMethod.POST,
                RequestMethod.PUT }, originPatterns = { "*" })
public class FilebotExecutorRestController {

    @Autowired
    private FilebotExecutorService filebotExecutorService;

    @Autowired
    private FilebotService filebotService;

    @GetMapping
    public ResponseEntity<Page<FilebotExecution>> getAll(Pageable pageable,
            @RequestParam(required = false) String status, @RequestParam(required = false) String path) {
        return ResponseEntity.ok(filebotExecutorService.getAllPageable(pageable, status, path));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilebotExecution> getById(@PathVariable String id) {
        return ResponseEntity.ok(filebotExecutorService.getById(id));
    }

    @GetMapping("/files")
    public ResponseEntity<List<String>> getListOfPaths() {
        return ResponseEntity.ok(filebotExecutorService.getAllFiles());
    }

    @PostMapping("/execute")
    public ResponseEntity<?> execute() {
        log.info("Manual execution");
        filebotService.execute();
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/execute/movie")
    public ResponseEntity<?> executeMovieManual() {
        log.info("Manual execution");
        filebotExecutorService.createBatchExecutionForMovie();
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping("/execute/show")
    public ResponseEntity<?> executeShowManual() {
        log.info("Manual execution");
        filebotExecutorService.createBatchExecutionForShow();
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<FilebotExecution> createNewExecution(@RequestBody QbittorrentModel qbittorrentModel) {
        log.info("Received qbittorrentModel: {}", qbittorrentModel);
        return ResponseEntity.ok(filebotExecutorService.createNewExecution(qbittorrentModel));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FilebotExecution> editExecution(@PathVariable String id,
            @RequestBody FilebotExecution filebotExecution) {
        log.info("Received edit for ID: {}", id);
        return ResponseEntity.ok(filebotExecutorService.editExecution(id, filebotExecution));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Received delete for ID: {}", id);
        filebotExecutorService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
