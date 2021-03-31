/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.transform;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.response.Response;

public class BlackDuckJsonTransformer {
    private final Gson gson;
    private final ObjectMapper objectMapper;
    private final IntLogger logger;

    public BlackDuckJsonTransformer(Gson gson, ObjectMapper objectMapper, IntLogger logger) {
        this.gson = gson;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    public <T extends BlackDuckResponse> T getResponse(Response response, Class<T> clazz) throws IntegrationException {
        String json = response.getContentString();
        return getResponseAs(json, clazz);
    }

    public <T extends BlackDuckResponse> T getResponseAs(String json, Class<T> clazz) throws BlackDuckIntegrationException {
        try {
            JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
            return getResponseAs(jsonElement, clazz);
        } catch (JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided json with Gson:%s%s", System.lineSeparator(), json));
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public <T extends BlackDuckResponse> T getResponseAs(JsonElement jsonElement, Class<T> clazz) throws BlackDuckIntegrationException {
        String json = gson.toJson(jsonElement);
        try {
            T blackDuckResponse = gson.fromJson(jsonElement, clazz);

            if (blackDuckResponse.hasSubclasses()) {
                // when a response can be subclassed, it will use its own state to
                // determine the specific subclass that should be used
                Class<? extends BlackDuckResponse> subclass = blackDuckResponse.getSubclass();
                BlackDuckResponse subclassResponse = gson.fromJson(jsonElement, subclass);
                blackDuckResponse = (T) subclassResponse;
            }

            blackDuckResponse.setGson(gson);
            blackDuckResponse.setJsonElement(jsonElement);
            blackDuckResponse.setJson(json);
            setPatch(blackDuckResponse);

            return blackDuckResponse;
        } catch (JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided jsonElement with Gson:%s%s", System.lineSeparator(), json));
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public <T extends BlackDuckResponse> BlackDuckPageResponse<T> getResponses(String json, Class<T> clazz) throws IntegrationException {
        try {
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            int totalCount = jsonObject.get("totalCount").getAsInt();
            JsonArray items = jsonObject.get("items").getAsJsonArray();
            List<T> itemList = new ArrayList<>();
            for (JsonElement jsonElement : items) {
                itemList.add(getResponseAs(jsonElement, clazz));
            }

            return new BlackDuckPageResponse<>(totalCount, itemList);
        } catch (JsonSyntaxException e) {
            logger.error(String.format("Could not parse the provided json responses with Gson:%s%s", System.lineSeparator(), json));
            throw new BlackDuckIntegrationException(e.getMessage(), e);
        }
    }

    public String producePatchedJson(BlackDuckResponse blackDuckResponse) {
        String lossyJson = gson.toJson(blackDuckResponse);
        try {
            JsonNode target = objectMapper.readTree(lossyJson);
            JsonNode patch = blackDuckResponse.getPatch();

            List<JsonNode> listOfPatches = transformPatchToListOfPatches(patch);
            for (JsonNode singleChangePatch : listOfPatches) {
                try {
                    target = JsonPatch.apply(singleChangePatch, target);
                } catch (Exception e) {
                    logger.warn("Could not apply a particular change - this may not be an issue if change involves an object that wasn't being updated: " + e.getMessage());
                }
            }

            StringWriter stringWriter = new StringWriter();
            objectMapper.writeValue(stringWriter, target);

            return stringWriter.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<JsonNode> transformPatchToListOfPatches(JsonNode patch) {
        List<JsonNode> listOfPatches = new ArrayList<>();

        Iterator<JsonNode> patchIterator = patch.iterator();
        while (patchIterator.hasNext()) {
            JsonNode change = patchIterator.next();

            ArrayNode changeArray = JsonNodeFactory.instance.arrayNode(1);
            changeArray.add(change);

            listOfPatches.add(changeArray);
        }

        return listOfPatches;
    }

    private void setPatch(BlackDuckResponse blackDuckResponse) {
        String lossyJson = gson.toJson(blackDuckResponse);

        try {
            JsonNode source = objectMapper.readTree(lossyJson);
            JsonNode target = objectMapper.readTree(blackDuckResponse.getJson());
            JsonNode patch = JsonDiff.asJson(source, target);
            blackDuckResponse.setPatch(patch);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
