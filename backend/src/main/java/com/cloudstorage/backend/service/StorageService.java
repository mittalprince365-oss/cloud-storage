package com.cloudstorage.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final HttpClient client = HttpClient.newHttpClient();

    // Supabase Storage pe file upload karo
    public String upload(MultipartFile file, String path) throws Exception {
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(uploadUrl))
            .header("Authorization", "Bearer " + serviceKey)
            .header("Content-Type", file.getContentType() != null ? file.getContentType() : "application/octet-stream")
            .POST(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Upload failed: " + response.body());
        }
        return path; // storage path wapas
    }

    // download ke liye public URL banao
    public String getPublicUrl(String path) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    // Supabase se file delete
    public void delete(String path) throws Exception {
        String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(deleteUrl))
            .header("Authorization", "Bearer " + serviceKey)
            .DELETE()
            .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}