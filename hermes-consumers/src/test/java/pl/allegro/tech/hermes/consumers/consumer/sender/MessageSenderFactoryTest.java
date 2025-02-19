package pl.allegro.tech.hermes.consumers.consumer.sender;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.googlecode.catchexception.CatchException;
import org.junit.Test;
import org.mockito.Mockito;
import pl.allegro.tech.hermes.api.EndpointAddress;
import pl.allegro.tech.hermes.api.Subscription;
import pl.allegro.tech.hermes.common.exception.EndpointProtocolNotSupportedException;

import java.util.Set;

import static com.googlecode.catchexception.CatchException.catchException;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.allegro.tech.hermes.test.helper.builder.SubscriptionBuilder.subscription;

public class MessageSenderFactoryTest {

    private MessageSender referenceMessageSender = Mockito.mock(MessageSender.class);

    @Test
    public void shouldCreateCustomProtocolMessageSender() {
        // given
        EndpointAddress endpoint = EndpointAddress.of("myProtocol://service");
        Subscription subscription = subscription("group.topic", "subscription", endpoint).build();
        MessageSenderFactory factory = new MessageSenderFactory(ImmutableList.of(
                protocolMessageSenderProviderReturning(referenceMessageSender, "myProtocol"))
        );

        // when
        MessageSender sender = factory.create(subscription);

        // then
        assertThat(sender).isEqualTo(referenceMessageSender);
    }

    @Test
    public void shouldGetProtocolNotSupportedExceptionWhenPassingUnknownUri() {
        // given
        MessageSenderFactory factory = new MessageSenderFactory(ImmutableList.of());
        Subscription subscription = subscription("group.topic", "subscription", "unknown://localhost:8080/test").build();

        // when
        catchException(factory).create(subscription);

        // then
        assertThat(CatchException.<EndpointProtocolNotSupportedException>caughtException())
                .isInstanceOf(EndpointProtocolNotSupportedException.class);
    }

    private ProtocolMessageSenderProvider protocolMessageSenderProviderReturning(Object createdMessageSender, String protocol) {
        return new ProtocolMessageSenderProvider() {
            @Override
            public MessageSender create(Subscription endpoint) {
                return (MessageSender) createdMessageSender;
            }

            @Override
            public Set<String> getSupportedProtocols() {
                return ImmutableSet.of(protocol);
            }

            @Override
            public void start() throws Exception {
            }

            @Override
            public void stop() throws Exception {
            }
        };
    }
}
