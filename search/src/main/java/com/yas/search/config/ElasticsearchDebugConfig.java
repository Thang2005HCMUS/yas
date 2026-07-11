// package com.yas.search.config;

// import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
// import org.apache.hc.core5.http.EntityDetails;
// import org.apache.hc.core5.http.HttpRequest;
// import org.apache.hc.core5.http.protocol.HttpContext;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.elasticsearch.client.ClientConfiguration;
// import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
// import org.springframework.data.elasticsearch.client.elc.rest5_client.Rest5Clients;

// @Configuration
// public class ElasticsearchDebugConfig extends ElasticsearchConfiguration {

//     private static final Logger log = LoggerFactory.getLogger(ElasticsearchDebugConfig.class);

//     @Override
//     public ClientConfiguration clientConfiguration() {
//         String u = System.getenv("ELASTICSEARCH_USERNAME") != null ? System.getenv("ELASTICSEARCH_USERNAME") : "yas";
//         String p = System.getenv("ELASTICSEARCH_PASSWORD") != null ? System.getenv("ELASTICSEARCH_PASSWORD") : "LarUmB3A49NTg9YmgW4=";

//         return ClientConfiguration.builder()
//                 .connectedTo("elasticsearch-es-http.elasticsearch:9200")
//                 .withBasicAuth(u, p)
//                 .withClientConfigurer(
//                     Rest5Clients.ElasticsearchHttpClientConfigurationCallback.from(
//                         (HttpAsyncClientBuilder httpClientBuilder) -> {
//                             httpClientBuilder.addRequestInterceptorFirst((HttpRequest request, EntityDetails entity, HttpContext context) -> {

//                                 log.info(">>>>>>> PRINTING OUTBOUND HTTP REQUEST TO ES >>>>>>>");
//                                 log.info("Request Line: {} {}", request.getMethod(), request.getRequestUri());

//                                 java.util.Arrays.stream(request.getHeaders()).forEach(header ->
//                                     log.info("Header -> {}: {}", header.getName(), header.getValue())
//                                 );
//                                 log.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
//                             });
//                             return httpClientBuilder;
//                         }
//                     )
//                 )
//                 .build();
//     }
// }