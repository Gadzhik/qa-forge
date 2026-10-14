package ru.qacourse.shop.files;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.qacourse.shop.common.ApiException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Загрузка и скачивание файлов (хранятся в памяти) — для уроков про multipart в REST Assured и Selenium. */
@RestController
@RequestMapping("/api/files")
@Tag(name = "Файлы")
public class FileController {

    public record StoredFile(String id, String name, String contentType, long size, Instant uploadedAt) {
    }

    private record Blob(StoredFile meta, byte[] data) {
    }

    private final Map<String, Blob> files = new ConcurrentHashMap<>();

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Загрузить файл (поле формы: file), не больше 5 МБ")
    public StoredFile upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw ApiException.badRequest("EMPTY_FILE", "Файл пустой");
        if (file.getSize() > 5 * 1024 * 1024) throw ApiException.badRequest("TOO_LARGE", "Файл больше 5 МБ");
        String id = UUID.randomUUID().toString();
        String type = file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType();
        StoredFile meta = new StoredFile(id, file.getOriginalFilename(), type, file.getSize(), Instant.now());
        files.put(id, new Blob(meta, file.getBytes()));
        return meta;
    }

    @GetMapping
    @Operation(summary = "Список загруженных файлов")
    public List<StoredFile> list() {
        return files.values().stream().map(Blob::meta).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Скачать файл")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        Blob blob = files.get(id);
        if (blob == null) throw ApiException.notFound("Файл " + id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(blob.meta().contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + blob.meta().name() + "\"")
                .body(blob.data());
    }

    public void clear() {
        files.clear();
    }
}
