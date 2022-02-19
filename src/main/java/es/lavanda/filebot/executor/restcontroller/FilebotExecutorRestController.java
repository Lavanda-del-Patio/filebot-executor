package es.lavanda.filebot.executor.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.service.FilebotExecutorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@CrossOrigin(allowedHeaders = "*", origins = { "http://localhost:4200", "https://lavandadelpatio.es",
        "https://pre.lavandadelpatio.es" }, allowCredentials = "true", exposedHeaders = "*", methods = {
                RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.PATCH, RequestMethod.POST,
                RequestMethod.PUT }, originPatterns = {})
@RequestMapping("/filebot-executor")
public class FilebotExecutorRestController {

    @Autowired
    private FilebotExecutorService filebotExecutorService;

    @GetMapping
    public ResponseEntity<Page<FilebotExecution>> getAll(Pageable pageable) {
        return ResponseEntity.ok(filebotExecutorService.getAllPageable(pageable));
    }

}
