package es.lavanda.filebot.executor.repository;

import org.springframework.stereotype.Repository;

import es.lavanda.filebot.executor.model.FilebotExecution;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;


@Repository
public interface FilebotExecutionRepository extends PagingAndSortingRepository<FilebotExecution, String> {

    boolean existsByFolderPath(String folderPath);
    
   Optional< FilebotExecution> findByFolderPath(String folderPath);
}
