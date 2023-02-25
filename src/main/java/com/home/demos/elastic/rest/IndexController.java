package com.home.demos.elastic.rest;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/index", produces = MediaType.APPLICATION_JSON_VALUE)
public class IndexController {

    RestClient restClient = RestClient.builder(
                    new HttpHost("localhost", 9200))
            .build();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY.MM.dd");


    @GetMapping
    public ResponseEntity<String> index(@RequestParam LocalDate date, @RequestParam String contentId) throws IOException {

        Request request = new Request("GET", "content-*/_search");
        request.setJsonEntity("{\"query\": {\"term\": {\"content_id\": \""+contentId+"\"}}}");
        Response response = restClient.performRequest(request);

        String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining());

        if(responseBody.contains("\"_index\":")) {
            String indexName = getIndexName(responseBody);
            String id = getDocumentId(responseBody);
            Request deleteRequest =  new Request("DELETE", indexName + "/_doc/" + id);
            restClient.performRequest(deleteRequest);
        }

        Request createRequest = new Request("POST", "content-"+date.format(formatter)+"/_doc");
        createRequest.setJsonEntity("{\"content_id\": \""+contentId+"\"}");
        Response createResponse = restClient.performRequest(createRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new BufferedReader(new InputStreamReader(createResponse.getEntity().getContent())).lines().collect(Collectors.joining())
        );
    }

    private String getDocumentId(String responseBody) {
        int i = responseBody.indexOf("\"_id\":");
        int j = responseBody.indexOf(',', i);

        return responseBody.substring(i + 7, j - 1);
    }

    private String getIndexName(String responseBody) {
        int i = responseBody.indexOf("\"_index\":");
        int j = responseBody.indexOf(',', i);

        return responseBody.substring(i + 10, j - 1);
    }
}
