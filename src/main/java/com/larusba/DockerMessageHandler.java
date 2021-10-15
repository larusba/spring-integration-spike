package com.larusba;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

public class DockerMessageHandler implements MessageHandler {

    private DockerClient dockerClient;

    DockerMessageHandler(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        CreateContainerResponse container = dockerClient
                .createContainerCmd("myownscript:1.0.0")
                .exec();

        dockerClient.startContainerCmd(container.getId()).exec();
    }
}
