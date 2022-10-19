package es.lavanda.filebot.executor.service;

import es.lavanda.filebot.executor.model.FilebotExecution;
import es.lavanda.lib.common.model.FilebotExecutionODTO;

public interface FilebotService {

    void execute();

    void resolution(FilebotExecutionODTO filebotExecutionODTO);

}