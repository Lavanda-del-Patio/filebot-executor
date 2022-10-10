package es.lavanda.filebot.executor.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.model.QbittorrentModel;
import es.lavanda.filebot.executor.service.FilebotExecutorService;
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

    @GetMapping
    public ResponseEntity<Page<FilebotExecution>> getAll(Pageable pageable) {
        return ResponseEntity.ok(filebotExecutorService.getAllPageable(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilebotExecution> getById(@PathVariable String id) {
        return ResponseEntity.ok(filebotExecutorService.getById(id));
    }

    @PostMapping
    public ResponseEntity<FilebotExecution> createNewExecution(@RequestBody QbittorrentModel qbittorrentModel) {
        log.info("Reciveid qbittorrentModel: {}", qbittorrentModel);
        return ResponseEntity.ok(filebotExecutorService.createNewExecution(qbittorrentModel));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FilebotExecution> reExecution(@PathVariable String id) {
        log.info("Reciveid reExectution for ID: {}", id);
        return ResponseEntity.ok(filebotExecutorService.reExecution(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Reciveid delete for ID: {}", id);
        filebotExecutorService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
