package es.lavanda.filebot.executor.repository;

import org.springframework.stereotype.Repository;

import es.lavanda.filebot.executor.model.FilebotExecution;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

@Repository
public interface FilebotExecutionRepository extends PagingAndSortingRepository<FilebotExecution, String> {

    boolean existsByPath(String folderPath);

    Optional<FilebotExecution> findByPath(String folderPath);

    Page<FilebotExecution> findAllByOrderByLastModifiedAtDesc(Pageable pageable);

    Page<FilebotExecution> findAllByStatusAndPathContainingIgnoreCaseOrderByLastModifiedAtDesc(Pageable pageable, String status, String path);

    Page<FilebotExecution> findAllByStatusOrderByLastModifiedAtDesc(Pageable pageable, String status);

    Page<FilebotExecution> findAllByPathIgnoreCaseContainingOrderByLastModifiedAtDesc(Pageable pageable, String path);

    List<FilebotExecution> findByStatusIn(List<String> status);

}
