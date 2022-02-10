package es.lavanda.filebot.executor.model;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.ToString;

@Data
@Document("filebot_execution")
@ToString
public class FilebotExecution {

    @Id
    private String id;

    @Field("file_name")
    private List<String> filesName;

    @Field("new_file_name")
    private List<String> newFilesName;

    @Field("folder_path")
    private String folderPath;

    @Field("new_folder_path")
    private String newFolderPath;

    @Field("command")
    private String command;

    @Field("status")
    private FilebotStatus status;

    @CreatedDate
    @Field("created_at")
    private Date createdAt;

    @LastModifiedDate
    @Field("last_modified_at")
    private Date lastModifiedAt;

    public enum FilebotStatus {

        UNPROCESSED, PROCESSING, PROCESSED, ERROR;

    }

}
