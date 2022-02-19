package es.lavanda.filebot.executor.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.filebot.executor.repository.FilebotExecutionRepository;
import es.lavanda.filebot.executor.service.FilebotExecutorService;

@Service
public class FilebotExecutorServiceImpl implements FilebotExecutorService {

    @Autowired
    private FilebotExecutionRepository filebotExecutionRepository;

    @Override
    public Page<FilebotExecution> getAllPageable(Pageable pageable) {
        return filebotExecutionRepository.findAll(pageable);
    }

    
}
