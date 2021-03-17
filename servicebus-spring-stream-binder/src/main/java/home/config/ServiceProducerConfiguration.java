// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package home.config;

import com.microsoft.azure.servicebus.Message;
import home.ServiceBusQueueBinderApplication;
import home.converter.AzureMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.converter.MessageConverter;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Configuration
@Profile("!manual")
public class ServiceProducerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusQueueBinderApplication.class);

    private int i = 0;

    @Bean
    public Supplier<Message> supply() {
        return () -> {
            LOGGER.info("Sending message, sequence " + i);
            Message message = new Message("Hello world, " + i++);
            String sessionId = "session1";
            message.setSessionId(sessionId);
            message.setPartitionKey(sessionId);
            return message;
        };
    }

    @Bean
    public Consumer<Message> consume() {
        return message -> {
            LOGGER.info("New message received: '{}'", message);
        };
    }

    @Bean
    public MessageConverter azureMessageConverter() {
        return new AzureMessageConverter();
    }
}