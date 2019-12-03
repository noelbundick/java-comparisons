package com.noelbundick.comparisons.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noelbundick.comparisons.search.models.ElasticsearchHotel;
import com.noelbundick.comparisons.search.models.HotelAddress;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class ElasticsearchHandler implements SearchHandler {

    private final RestHighLevelClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public ElasticsearchHandler(String endpoint) {
        client = new RestHighLevelClient(RestClient.builder(HttpHost.create(endpoint)));
    }

    @Override
    // Trying to issue a request against an unspecified index will throw an error that needs to be handled
    public Mono<ServerResponse> errorHandling(ServerRequest request) {
        IndexRequest bogusRequest = new IndexRequest();

        return Mono.<IndexResponse>create(sink -> client.indexAsync(bogusRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .flatMap(res -> ok().bodyValue(String.format("Indexed document with seq no: %d", res.getSeqNo())))
            .onErrorResume(err -> ServerResponse.status(500).bodyValue("Unexpected error: " + err.toString()));
    }

    @Override
    // There are different methods for single vs bulk (preferred) indexing documents
    public Mono<ServerResponse> indexing(ServerRequest request) {
        ElasticsearchHotel hotel = new ElasticsearchHotel()
            .hotelId(UUID.randomUUID().toString())
            .address(new HotelAddress().city("Seattle"));
        String json = serialize(hotel);

        IndexRequest indexRequest = new IndexRequest("hotels")
            .id(hotel.hotelId())
            .source(json, XContentType.JSON);

        return Mono.<IndexResponse>create(sink -> client.indexAsync(indexRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .flatMap(res -> ok().bodyValue(res));
    }

    @Override
    // Compile index operations and send all at once
    public Mono<ServerResponse> bulkIndexing(ServerRequest request) {
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < 10000; i++) {
            ElasticsearchHotel hotel = new ElasticsearchHotel()
                .hotelId(Integer.toString(i))
                .address(new HotelAddress().city("Seattle"));

            bulkRequest.add(new IndexRequest("hotels")
                .id(hotel.hotelId())
                .source(serialize(hotel), XContentType.JSON));
        }

        return Mono.<BulkResponse>create(sink -> client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .flatMap(res -> ok().bodyValue(String.format("Indexed %d documents", res.getItems().length)));
    }

    @Override
    public Mono<ServerResponse> clear(ServerRequest request) {
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest("hotels");
        return Mono.<AcknowledgedResponse>create(sink -> client.indices().deleteAsync(deleteRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .flatMap(res -> ok().bodyValue("index deleted"));
    }

    @Override
    public Mono<ServerResponse> count(ServerRequest request) {
        CountRequest countRequest = new CountRequest("hotels");
        return Mono.<CountResponse>create(sink -> client.countAsync(countRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .flatMap(res -> ok().bodyValue(res.getCount()));
    }

    @Override
    public Mono<ServerResponse> search(ServerRequest request) {
        SearchRequest searchRequest = new SearchRequest("hotels");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
            .query(new QueryStringQueryBuilder("Seattle"));
        searchRequest.source(sourceBuilder);

        return Mono.<SearchResponse>create(sink -> client.searchAsync(searchRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .flatMap(res -> ok().bodyValue(res.getHits()));
    }

    @Override
    public Mono<ServerResponse> searchWithPaging(ServerRequest request) {
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1));

        SearchRequest searchRequest = new SearchRequest("hotels");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
            .query(new QueryStringQueryBuilder("Seattle"))
            .size(5);
        searchRequest.source(sourceBuilder)
            .scroll(scroll);

        return Mono.<SearchResponse>create(sink -> client.searchAsync(searchRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .expand(res -> {
                if (res.getHits()==null || res.getHits().getHits().length==0) {
                    return Mono.empty();
                }

                SearchScrollRequest scrollRequest = new SearchScrollRequest(res.getScrollId())
                    .scroll(scroll);
                return Mono.create(sink -> client.scrollAsync(scrollRequest, RequestOptions.DEFAULT, getListenerForSink(sink)));
            })
            .reduce(new ArrayList<SearchHit>(), (list, res) -> {
                list.addAll(Arrays.asList(res.getHits().getHits()));
                return list;
            })
            .flatMap(list -> ok().bodyValue(list));
    }

    @Override
    public Mono<ServerResponse> searchWithFacets(ServerRequest request) {
        SearchRequest searchRequest = new SearchRequest("hotels");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
            .aggregation(AggregationBuilders.terms("Category"))
            .aggregation(AggregationBuilders.terms("Rating"))
            .aggregation(AggregationBuilders.terms("ParkingIncluded"))
            .aggregation(AggregationBuilders.terms("Rooms.SmokingAllowed"));
        searchRequest.source(sourceBuilder);

        return Mono.<SearchResponse>create(sink -> client.searchAsync(searchRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .map(res -> res.getAggregations().asList())
            .flatMap(list -> ok().bodyValue(list));
    }

    @Override
    public Mono<ServerResponse> searchWithCustomTypes(ServerRequest request) {
        SearchRequest searchRequest = new SearchRequest("hotels");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder()
            .query(new QueryStringQueryBuilder("Seattle"))
            .size(5);
        searchRequest.source(sourceBuilder);

        return Mono.<SearchResponse>create(sink -> client.searchAsync(searchRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .flatMap(res -> {
                List<ElasticsearchHotel> hotels = Arrays.stream(res.getHits().getHits())
                    .map(item -> deserialize(item.getSourceAsString(), ElasticsearchHotel.class))
                    .collect(Collectors.toList());
                return ok().bodyValue(hotels);
            });
    }

    @Override
    public Mono<ServerResponse> indexManagement(ServerRequest request) {
        CreateIndexRequest createRequest = new CreateIndexRequest("hotels");
        return Mono.<CreateIndexResponse>create(sink -> client.indices().createAsync(createRequest, RequestOptions.DEFAULT, getListenerForSink(sink)))
            .flatMap(res -> ok().bodyValue(String.format("index created: %s", res.index())));
    }

    @Override
    public Mono<ServerResponse> reset(ServerRequest request) {
        return clear(request)
            .onErrorResume(ElasticsearchException.class, err -> ok().bodyValue("ignored"))
            .flatMap(res -> indexManagement(request))
            .flatMap(res -> uploadHotels())
            .flatMap(res -> ok().bodyValue(String.format("reset docs: %s", res.getItems().length)));
    }

    private Mono<BulkResponse> uploadHotels() {
        try {
            URL filePath = getClass().getClassLoader().getResource("search-hotels-elasticsearch.json");
            List<Map<String, ?>> hotels = mapper.readValue(filePath, new TypeReference<>() {
            });

            BulkRequest bulkRequest = new BulkRequest();
            for (Map<String, ?> hotel : hotels) {
                bulkRequest.add(new IndexRequest("hotels")
                    .id((String) hotel.get("HotelId"))
                    .source(hotel));
            }

            return Mono.create(sink -> client.bulkAsync(bulkRequest, RequestOptions.DEFAULT, getListenerForSink(sink)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Utility method to convert ActionListener async callback to Mono<>
    private <T> ActionListener<T> getListenerForSink(MonoSink<T> sink) {
        return new ActionListener<>() {
            @Override
            public void onResponse(T response) {
                sink.success(response);
            }

            @Override
            public void onFailure(Exception e) {
                sink.error(e);
            }
        };
    }

    // Utility to serialize objects to JSON
    // Convert JSON errors to RuntimeException for hacking around
    private String serialize(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // Utility to deserialize objects from JSON
    // Convert JSON errors to RuntimeException for hacking around
    private <T> T deserialize(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
