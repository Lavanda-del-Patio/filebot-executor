package es.lavanda.filebot.executor.service;

import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.lib.common.model.FilebotExecutionODTO;
import es.lavanda.lib.common.model.QbittorrentModel;

public interface FilebotService {

    void execute();

    void execute(String id);

    void resolutionTelegramBot(FilebotExecutionODTO filebotExecutionODTO);

}