package es.lavanda.filebot.executor.service;

import java.util.List;

public interface FileService {

    List<String> ls(String path);

    void rmdir(String path);
}
