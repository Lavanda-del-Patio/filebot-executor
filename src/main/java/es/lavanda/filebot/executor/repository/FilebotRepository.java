package es.lavanda.filebot.executor.repository;

import org.springframework.stereotype.Repository;

import es.lavanda.filebot.executor.model.Filebot;

import org.springframework.data.repository.PagingAndSortingRepository;


@Repository
public interface FilebotRepository extends PagingAndSortingRepository<Filebot, String> {

    
}
