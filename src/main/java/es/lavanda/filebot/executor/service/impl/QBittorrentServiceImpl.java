package es.lavanda.filebot.executor.service.impl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.lavanda.filebot.executor.exception.FilebotExecutorException;
import es.lavanda.filebot.executor.model.QbittorrentInfo;
import es.lavanda.filebot.executor.service.QBittorrentService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class QBittorrentServiceImpl implements QBittorrentService {
    private final OkHttpClient client = new OkHttpClient();

    @Value("${qbittorrent.url}")
    private String QB_URL;

    private final String API_ENDPOINT = "/api/v2/torrents/info";
    private final String STATUS_COMPLETED = "completed";

    public List<QbittorrentInfo> getCompletedTorrents() throws IOException {
        Request request = new Request.Builder()
                .url(QB_URL + API_ENDPOINT + "?filter=" + STATUS_COMPLETED)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new FilebotExecutorException("Unexpected code " + response);
            }
            String jsonResponse = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonResponse, new TypeReference<List<QbittorrentInfo>>() {
            });
        }
    }
}
