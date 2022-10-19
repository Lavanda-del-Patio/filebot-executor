package es.lavanda.filebot.executor.repository;

import org.springframework.stereotype.Repository;

import es.lavanda.filebot.executor.model.FilebotExecution;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Repository
public interface FilebotExecutionRepository extends PagingAndSortingRepository<FilebotExecution, String> {

    boolean existsByPath(String folderPath);

    Optional<FilebotExecution> findByPath(String folderPath);

    Page<FilebotExecution> findAllByOrderByLastModifiedAtDesc(Pageable pageable);

    Optional<FilebotExecution> findOneByStatus(String status, String status2);

}
