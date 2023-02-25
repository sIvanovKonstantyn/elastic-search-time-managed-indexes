package com.home.demos.elastic.rest;

import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
public class SearchController {

    RestClient restClient = RestClient.builder(
                    new HttpHost("localhost", 9200))
            .build();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY.MM");


    @GetMapping
    public ResponseEntity<String> search(@RequestParam Integer numberOfDays, @RequestParam String contentId) throws IOException {

        Request request = new Request("GET", buildIndexNames(numberOfDays) + "/_search");
        request.setJsonEntity("{\"query\": {\"term\": {\"content_id\": \"" + contentId + "\"}}}");
        Response response = restClient.performRequest(request);

        String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining());

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    private String buildIndexNames(Integer numberOfDays) {
        StringJoiner sj = new StringJoiner(",");

        LocalDate now = LocalDate.now();
        LocalDate prev = now.minus(numberOfDays, ChronoUnit.DAYS);

        int i = prev.getMonthValue();
        int j = now.getMonthValue();
        while (i++ <= j) {
            sj.add("content-" + prev.format(formatter) + "*");
            prev = prev.plusMonths(1);
        }

        return sj.toString();
    }
}
