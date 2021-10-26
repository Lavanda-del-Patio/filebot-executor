package es.lavanda.filebot.parser.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import es.lavanda.filebot.parser.model.FilebotFile;


@Repository
public interface FilebotFileRepository extends PagingAndSortingRepository<FilebotFile, String> {
    
}
