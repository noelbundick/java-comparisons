package com.noelbundick.comparisons.storage;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class AzureStorageHandler {
    private static ParameterizedTypeReference<Map<String, List<BlobItem>>> listAllBlobsTypeRef = new ParameterizedTypeReference<>() {};

    private final BlobServiceAsyncClient serviceClient;

    public AzureStorageHandler(String connectionString) {
        serviceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();
    }

    /**
     * List all containers in a Storage Account
     */
    public Mono<ServerResponse> listContainers(ServerRequest request) {
        Flux<BlobContainerItem> containers = serviceClient.listBlobContainers();
        return ok().body(containers, BlobContainerItem.class);
    }

    /**
     * List all blobs in all containers in a Storage Account
     */
    public Mono<ServerResponse> listAllBlobs(ServerRequest request) {
        Mono<Map<String, List<BlobItem>>> results = serviceClient.listBlobContainers()
            .flatMap(c -> {
                String containerName = c.getName();
                BlobContainerAsyncClient containerClient = serviceClient.getBlobContainerAsyncClient(containerName);
                return containerClient.listBlobs().map(b -> new AbstractMap.SimpleEntry<>(containerName, b));
            })
            .reduce(new HashMap<>(), (map, entry) -> {
                String containerName = entry.getKey();
                BlobItem blob = entry.getValue();

                map.computeIfAbsent(containerName, x -> new ArrayList<>())
                    .add(blob);
                return map;
            });

        return ok().body(results, listAllBlobsTypeRef);
    }
}
