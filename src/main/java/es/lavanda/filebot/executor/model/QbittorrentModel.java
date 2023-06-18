package es.lavanda.filebot.executor.model;

import java.nio.file.Path;

import lombok.Data;

@Data
public class QbittorrentModel {

    private String id;

    private String category;

    private Path name;

    private String action;

}
