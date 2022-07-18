package es.lavanda.filebot.executor.model;

import java.nio.file.Path;

import lombok.Data;

@Data
public class QbittorrentModel {
    private String name;

    private String category;

    private Path path;

}
