package es.lavanda.filebot.executor.model;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.ToString;

@Data
@Document("filebot_execution")
@ToString
public class FilebotExecution implements Serializable {

    @Id
    private String id;

    // @Field("file_name")
    // private List<String> filesName = new ArrayList<>();

    // @Field("new_file_name")
    // private List<String> newFilesName = new ArrayList<>();

    // @Field("folder_path")
    // private String folderPath;

    // @Field("parent_folder_path")
    // private String parentFolderPath;

    // @Field("new_parent_folder_path")
    // private String newParentFolderPath;

    @Field("files")
    private List<String> files = new ArrayList<>();

    @Field("new_files")
    private List<String> newFiles = new ArrayList<>();

    @Field("path")
    private String path;

    @Field("new_path")
    private String newPath;

    @Field("parent_path")
    private String parentPath;

    @Field("new_parent_path")
    private String newParentPath;

    @Field("category")
    private String category;

    @Field("command")
    private String command;

    @Field("status")
    private FilebotStatus status = FilebotStatus.UNPROCESSED;

    @CreatedDate
    @Field("created_at")
    private Date createdAt;

    @LastModifiedDate
    @Field("last_modified_at")
    private Date lastModifiedAt;

    @Indexed(expireAfter = "P14D")
    private String expireAfterFourteenDays;

    public enum FilebotStatus {

        UNPROCESSED, PROCESSING, PROCESSED, PROCESSED_EXISTED, ERROR, FILES_NOT_FOUND;

    }

}
