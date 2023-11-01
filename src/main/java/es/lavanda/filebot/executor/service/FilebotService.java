package es.lavanda.filebot.executor.service;

import es.lavanda.lib.common.model.FilebotExecutionODTO;

public interface FilebotService {

    void execute();

    void execute(String id);

    void resolutionTelegramBot(FilebotExecutionODTO filebotExecutionODTO);

}