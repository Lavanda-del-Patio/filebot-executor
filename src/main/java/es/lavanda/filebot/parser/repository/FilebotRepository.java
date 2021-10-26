package es.lavanda.filebot.parser.repository;

import org.springframework.stereotype.Repository;

import es.lavanda.filebot.parser.model.Filebot;

import org.springframework.data.repository.PagingAndSortingRepository;


@Repository
public interface FilebotRepository extends PagingAndSortingRepository<Filebot, String> {

    
}
