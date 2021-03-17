package home.converter;

import com.microsoft.azure.servicebus.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;

public class AzureMessageConverter extends AbstractMessageConverter {

    public AzureMessageConverter() {
        super(new MimeType("application", "azure-message"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Message.class.equals(clazz);
    }

    @Override
    protected Object convertFromInternal(org.springframework.messaging.Message<?> message, Class<?> targetClass, Object conversionHint) {
        Object payload = message.getPayload();
        Message result = payload instanceof Message ? (Message) payload : new Message((byte[]) payload);
        String sessionId = message.getHeaders().get("sessionId", String.class);
        result.setSessionId(sessionId);
        result.setPartitionKey(sessionId);
        return result;
    }

    @Override
    protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
        return payload;
    }


}