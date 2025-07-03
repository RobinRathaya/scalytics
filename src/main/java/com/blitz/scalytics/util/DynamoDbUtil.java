package com.blitz.scalytics.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.blitz.scalytics.model.ReportData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Component
@RequiredArgsConstructor
public class DynamoDbUtil {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table}")
    private String tableName;

    @Value("${aws.dynamodb.report.id}")
    private String reportId;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void saveReportData(ReportData reportData) {
        try {
            // delete existing report data if any
            deleteReportData();
            String json = objectMapper.writeValueAsString(reportData);

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("reportId", AttributeValue.builder().s(reportId).build());
            item.put("reportJson", AttributeValue.builder().s(json).build());

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

                dynamoDbClient.putItem(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize ReportData", e);
        }
    }

    public ReportData fetchReportData() {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("reportId", AttributeValue.builder().s(reportId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (response.hasItem()) {
            String json = response.item().get("reportJson").s();
            try {
                return objectMapper.readValue(json, ReportData.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize ReportData", e);
            }
        } else {
            return null;
        }
    }

    public void deleteReportData() {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("reportId", AttributeValue.builder().s(reportId).build());

        dynamoDbClient.deleteItem(builder -> builder
                .tableName(tableName)
                .key(key));
    }
}