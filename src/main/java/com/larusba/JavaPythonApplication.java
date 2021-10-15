package com.larusba;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.neo4j.driver.SessionConfig;
import org.neo4j.jdbc.Neo4jDataSource;
import org.neo4j.jdbc.Neo4jDriver;
import org.neo4j.jdbc.bolt.BoltNeo4jDataSource;
import org.neo4j.jdbc.bolt.DriverFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.*;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.integration.scripting.dsl.Scripts;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.messaging.MessageHandler;

import java.time.Duration;

@SpringBootApplication
public class JavaPythonApplication {
    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext ctx = SpringApplication
                .run(JavaPythonApplication.class, args);

        System.out.println("Hit 'Enter' to terminate");
        System.in.read();
        ctx.close();
    }

    @Bean
    public MessageSource<Object> jdbcMessageSource() {
        return new JdbcPollingChannelAdapter(
                new DriverManagerDataSource("jdbc:neo4j:bolt://localhost:11006", "neo4j", "password"),
                "MATCH (n) WHERE n.at <= timestamp() AND n.at >= timestamp() - 100 RETURN n"
        );
    }

    @Bean
    public IntegrationFlow pollingFlow() {
        return IntegrationFlows.from(jdbcMessageSource(), c -> c.poller(Pollers.fixedRate(100).maxMessagesPerPoll(1)))
                .transform(Object::toString)
                .channel("input")
                .get();
    }

    @Bean
    public IntegrationFlow toPythonFlow() {
        return IntegrationFlows.from("input")
                .handle(Scripts.processor("/myownscript.py").lang("python").refreshCheckDelay(1000))
                .get();
    }

//    @Bean
//    public IntegrationFlow toDockerFlow() {
//        return IntegrationFlows.from("input")
//                .handle(startContainer())
//                .get();
//    }
//
//    @Bean
//    public MessageHandler startContainer() {
//        DefaultDockerClientConfig config = DefaultDockerClientConfig
//                .createDefaultConfigBuilder()
//                .build();
//
//        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
//                .dockerHost(config.getDockerHost())
//                .sslConfig(config.getSSLConfig())
//                .maxConnections(100)
//                .connectionTimeout(Duration.ofSeconds(30))
//                .responseTimeout(Duration.ofSeconds(45))
//                .build();
//
//        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
//        return new DockerMessageHandler(dockerClient);
//    }
}
