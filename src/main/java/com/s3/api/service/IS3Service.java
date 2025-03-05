package com.s3.api.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface IS3Service {
    //creat bucket en s3
    String createBucket(String bucketName);

    //saber si un bucket existe
    String checkIfBucketExist(String bucketName);

    //Listar buckets
    List<String> getAllBuckets();

    //Cargar un archivo a un bucket
    Boolean uploadFile(String bucketName, String key, Path fileLocation);

    //Descargar un archivo de un bucet
    void downloadFile(String bucketName, String key) throws IOException;

    //Generar Url prefirmada para subir archivos
    //son urls que nosotros generamos para conceder permiso temporal auna app o a un usuario
    String generatePresignedUploadUrl(String bucketName, String key, Duration duration);

    //Generar Url prefirmada para descargar archivos
    String generatePresignedDownloadUrl(String bucketName, String key, Duration duration);
}
