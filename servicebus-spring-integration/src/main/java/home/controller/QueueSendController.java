package home.controller;

import com.azure.spring.integration.core.DefaultMessageHandler;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.integration.servicebus.queue.ServiceBusQueueOperation;
import com.microsoft.azure.servicebus.Message;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
public class QueueSendController {

    private static final Log LOGGER = LogFactory.getLog(QueueSendController.class);
    private static final String OUTPUT_CHANNEL = "queue.output";
    private static final String QUEUE_NAME = "<>";

    @Autowired
    private QueueOutboundGateway messagingGateway;

    /**
     * Posts a message to a Service Bus Queue
     */
    @PostMapping("/queues")
    public ResponseEntity<Void> send(@RequestParam("message") String text, @RequestParam("sessionId") String sessionId) {
        Message message = new Message(text);
        message.setSessionId(sessionId);
        log.info("Sending message '{}' with session '{}'", text, sessionId);
        this.messagingGateway.send(message);
        return ResponseEntity.accepted().build();
    }

    @Bean
    @ServiceActivator(inputChannel = OUTPUT_CHANNEL)
    public MessageHandler queueMessageSender(ServiceBusQueueOperation queueOperation) {
        ServiceBusClientConfig clientConfig = ServiceBusClientConfig.builder()
                .setSessionsEnabled(true)
                .build();
        queueOperation.setClientConfig(clientConfig);

        DefaultMessageHandler handler = new DefaultMessageHandler(QUEUE_NAME, queueOperation);
        handler.setSendCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(Void result) {
                LOGGER.info("Message was sent successfully.");
            }

            @Override
            public void onFailure(Throwable ex) {
                LOGGER.info("There was an error sending the message.");
            }
        });

        return handler;
    }

    @MessagingGateway(defaultRequestChannel = OUTPUT_CHANNEL)
    private interface QueueOutboundGateway {
        void send(Message text);
    }
}
