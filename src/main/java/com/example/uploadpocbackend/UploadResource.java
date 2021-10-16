package com.example.uploadpocbackend;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RestController
public class UploadResource {

    private static final List<String> ACCEPTED_FILETYPES = List.of("jpg", "jpeg", "gif", "png", "apng", "svg", "bmp");

    @Value("${server.static.content.path}")
    private String staticContentPath;

    // @RequestParam String userId should be replaced with user session token in prod
    @PostMapping(value = "/upload", consumes = TEXT_PLAIN_VALUE)
    public void uploadContent(@RequestParam String type, @RequestParam UUID userId, @RequestBody String content) {
        String lowerType = type.toLowerCase(Locale.ROOT);
        if (!ACCEPTED_FILETYPES.contains(lowerType)) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        try {
            for (File file : allFilesWithPathPrefix(userId.toString(), staticContentPath)) {
                file.delete();
            }

            String filename = Paths.get(staticContentPath, userId.toString()) + "." + lowerType;
            log.info("url = {}", "http://localhost/static/" + userId + "." + lowerType);
            byte[] contentBytes = Base64.getDecoder().decode(content);
            FileOutputStream writer = new FileOutputStream(filename);
            writer.write(contentBytes);
            writer.close();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "upload/supported", produces = APPLICATION_JSON_VALUE)
    public List<String> supportedTypes() {
        return ACCEPTED_FILETYPES;
    }

    private File[] allFilesWithPathPrefix(String prefix, String dirPath) {
        return new File(dirPath).listFiles((ignored, name) -> name.startsWith(prefix));
    }
}
