package es.lavanda.filebot.executor.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import es.lavanda.filebot.executor.model.FilebotFile;


@Repository
public interface FilebotFileRepository extends PagingAndSortingRepository<FilebotFile, String> {
    
}
