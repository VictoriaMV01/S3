package com.s3.api.controller;

import com.s3.api.service.IS3Service;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("s3")
public class AppController {

    @Value("${spring.destination.folder}")
    private String destinationFolder;

    @Autowired
    IS3Service service;

    @PostMapping("/create")
    public ResponseEntity<String> createBucket(@RequestParam String bucketName){
        return ResponseEntity.ok(service.createBucket(bucketName));
    }

    @GetMapping("/check/{bucketName}")
    public ResponseEntity<String> checkBucket(@PathVariable String bucketName){
        return ResponseEntity.ok(service.checkIfBucketExist(bucketName));
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listBuckets(){
        return ResponseEntity.ok(service.getAllBuckets());
    }

    @PostMapping("/upload")

    public ResponseEntity<String> uploadFile(@RequestParam String bucketName,
                                             @RequestParam String key,
                                             @RequestPart MultipartFile file) throws IOException{
        try{
            Path staticDir = Paths.get(this.destinationFolder);
            if(!Files.exists(staticDir)){
                Files.createDirectories(staticDir);
            }

            Path filePath = staticDir.resolve(file.getOriginalFilename());
            Path finalPath = Files.write(filePath, file.getBytes());

            Boolean result = this.service.uploadFile(bucketName, key, finalPath);

            if(result){
                Files.delete(finalPath);
                return ResponseEntity.ok("Archivo cargado correctamente");
            }else{
                return  ResponseEntity.internalServerError().body("Error al cargar el arcghivo al buckt");
            }

        }catch (IOException e){
            throw new IOException("Error al procesar el archivo " + e.getCause());
        }
    }

    @PostMapping("/download")
    public ResponseEntity<String> downloadFile(@RequestParam String bucketName, @RequestParam String key) throws IOException {
        service.downloadFile(bucketName, key);
        return ResponseEntity.ok("Archivo descargado correctamente");
    }

    @PostMapping("/upload/presigned")
    public ResponseEntity<String> generatePresignedUploadUrl(@RequestParam String bucketName,
                                                             @RequestParam String key,
                                                             @RequestParam Long time){
        Duration liveDuration = Duration.ofMinutes(time);
        return ResponseEntity.ok(service.generatePresignedUploadUrl(bucketName,key,liveDuration));
    }

    @PostMapping("/download/presigned")
    public ResponseEntity<String> generatePresignedDownloadUrl(@RequestParam String bucketName,
                                                               @RequestParam String key,
                                                               @RequestParam Long time){
        Duration liveDuration = Duration.ofMinutes(time);
        return ResponseEntity.ok(service.generatePresignedDownloadUrl(bucketName,key,liveDuration));
    }
}
