package home.controller;

import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.Checkpointer;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.inbound.ServiceBusQueueInboundChannelAdapter;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.microsoft.azure.servicebus.MessageSession;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class QueueReceiveController {

    private static final String INPUT_CHANNEL = "queue.input";
    private static final String QUEUE_NAME = "<>";

    @ServiceActivator(inputChannel = INPUT_CHANNEL)
    public void messageReceiver(byte[] payload, @Header(AzureHeaders.CHECKPOINTER) Checkpointer checkpointer,
                                @Header(AzureHeaders.MESSAGE_SESSION) MessageSession session) {
        String message = new String(payload);
        String sessionId = session.getSessionId();
        log.info("New message '{}' received with session: '{}'", message, sessionId);
        checkpointer.success().handle((r, ex) -> {
            if (ex == null) {
                log.info("Message '{}' with session '{}' successfully checkpointed.", message, sessionId);
            }
            return null;
        });
    }

    @Bean
    public ServiceBusQueueInboundChannelAdapter queueMessageChannelAdapter(
            @Qualifier(INPUT_CHANNEL) MessageChannel inputChannel, ServiceBusQueueOperation queueOperation) {
        CheckpointConfig checkpointConfig = CheckpointConfig.builder()
                .checkpointMode(CheckpointMode.MANUAL)
                .build();
        ServiceBusClientConfig clientConfig = ServiceBusClientConfig.builder()
                .setSessionsEnabled(true)
                .build();
        queueOperation.setCheckpointConfig(checkpointConfig);
        queueOperation.setClientConfig(clientConfig);

        ServiceBusQueueInboundChannelAdapter adapter
                = new ServiceBusQueueInboundChannelAdapter(QUEUE_NAME, queueOperation);
        adapter.setOutputChannel(inputChannel);
        return adapter;
    }

    @Bean(name = INPUT_CHANNEL)
    public MessageChannel input() {
        return new DirectChannel();
    }
}
