package es.lavanda.filebot.executor.service;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import es.lavanda.filebot.executor.model.QbittorrentInfo;

public interface QBittorrentService {

    List<QbittorrentInfo> getCompletedTorrents() throws  IOException ;

}
