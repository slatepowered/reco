package slatepowered.reco.rmq;

import com.rabbitmq.client.*;
import lombok.Data;
import slatepowered.reco.BinaryCommunicationProvider;
import slatepowered.reco.Message;
import slatepowered.reco.ReceivedMessage;
import slatepowered.reco.Serializer;
import slatepowered.veru.misc.Throwables;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.logging.Logger;

@SuppressWarnings({ "rawtypes" })
public class RMQProvider extends BinaryCommunicationProvider<RMQChannel> {

    // logger
    static final Logger LOGGER = Logger.getLogger("RMQProvider");

    public static final String EXCHANGE_NAME      = "comm";
    public static final String PUBLISH_QUEUE_NAME = "pub";

    ////////////////////////////////////////

    // the RabbitMQ connection and channel
    Connection rmqConnection;
    Channel rmqChannel;

    public RMQProvider(String localName, Serializer serializer) {
        super(localName, serializer);
    }

    /**
     * Connect to the RabbitMQ instance at the
     * provided host and port. The credentials and
     * virtual host name are hard-coded.
     * @param host The hostname.
     * @param port The port (if -1 default is used).
     * @param username The username.
     * @param password The password.
     * @param virtualHost The virtual host name.
     */
    public RMQProvider connect(String host, int port, String username, String password,
                               String virtualHost) {
        try {
            // create connection
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            if (port >= 0) factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setVirtualHost(virtualHost);
            rmqConnection = factory.newConnection();

            // create channel
            rmqChannel = rmqConnection.createChannel();

            // declare communication exchange
            rmqChannel.exchangeDeclare(EXCHANGE_NAME, "topic");
        } catch (Exception e) {
            Throwables.sneakyThrow(e);
        }

        return this;
    }

    /**
     * Binds this connection and starts
     * listening for messages on both the local
     * queue and the publishing queue.
     */
    public RMQProvider bind() {
        try {
            // declare and bind local queue
            rmqChannel.queueDeclare(localName, false, false, false, null);
            rmqChannel.queueBind(localName, EXCHANGE_NAME, localName);

            // create listener on local queue
            rmqChannel.basicConsume(localName, true, (consumerTag, message) -> receiveRMQ(message, localName), consumerTag -> { });

            // declare and bind pub queue
            rmqChannel.queueDeclare(PUBLISH_QUEUE_NAME, false, false, false, null);
            rmqChannel.queueBind(PUBLISH_QUEUE_NAME, EXCHANGE_NAME, PUBLISH_QUEUE_NAME);

            // create listener on pub queue
            rmqChannel.basicConsume(PUBLISH_QUEUE_NAME, true, (consumerTag, message) -> receiveRMQ(message, PUBLISH_QUEUE_NAME), consumerTag -> { });
        } catch (Exception e) {
            LOGGER.warning("Failed to bind to local and pub queue");
            e.printStackTrace();
        }

        return this;
    }

    // handles received RMQ deliveries
    private void receiveRMQ(Delivery delivery, String queueName) {
        try {
            // decode message body and properties
            DecodeResult decodeResult = decodeReceivedMessage(delivery.getBody());
            assert decodeResult != null;
            ReceivedMessage<?> message = decodeResult.message;

            // call communication provider
            received(
                    message,
                    decodeResult.domain,
                    queueName,
                    decodeResult.sourceName
            );
        } catch (Exception e) {
            logger.warning("Failed to handle received msg cause(UncaughtError)");
            e.printStackTrace();
        }
    }

    // result record
    @Data
    static class DecodeResult {
        final Domain domain;
        final String sourceName;
        final ReceivedMessage<?> message;
    }

    // packs a message and its content
    // into a byte array with the appropriate
    // headers and identifier/name
    private byte[] packSendingMessage(Message message,
                                      Domain domain) {
        try {
            // create stream
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream dataStream = new DataOutputStream(byteStream);

            // write message name
            dataStream.writeUTF(message.getName());

            // write type and headers
            dataStream.writeByte(domain.getEncoded());
            dataStream.writeUTF(localName); // write source node

            // serialize message content
            serializer.write(dataStream, message.payload());

            byte[] bytes = byteStream.toByteArray();

            // return bytes
            return bytes;
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
            return null;
        }
    }

    // decodes a message from
    // the packed data
    private DecodeResult decodeReceivedMessage(byte[] bytes) {
        try {
            // create stream
            ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
            DataInputStream dataStream = new DataInputStream(byteStream);

            // read message name
            String name = dataStream.readUTF();

            // read type and headers
            byte type = dataStream.readByte();
            String sourceName = dataStream.readUTF();

            // deserialize message content
            ReceivedMessage<Object> message = new ReceivedMessage<>(name);
            message.payload(serializer.read(dataStream));

            // return result
            return new DecodeResult(Domain.getByEncoded(type), sourceName, message);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
            return null;
        }
    }

    // constant basic properties
    static final AMQP.BasicProperties C_BASIC_PROPERTIES = new AMQP.BasicProperties();

    /**
     * Sends the given message to a specific
     * target.
     *
     * @param message The message.
     * @param target The target.
     */
    public void send(Message message, String target) {
        try {
            // pack bytes
            byte[] bytes = packSendingMessage(message, Domain.DIRECT);

            // publish with routing key
            rmqChannel.basicPublish(EXCHANGE_NAME, target, C_BASIC_PROPERTIES, bytes);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
        }
    }

    /**
     * Sends the given message to a specific
     * auxiliary target.
     *
     * @param message The message.
     * @param target The aux target.
     */
    public void sendAux(Message message, String target) {
        try {
            // pack bytes
            byte[] bytes = packSendingMessage(message, Domain.AUX);

            // publish with routing key
            rmqChannel.basicPublish(EXCHANGE_NAME, target, C_BASIC_PROPERTIES, bytes);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
        }
    }

    /**
     * Publish a message to all nodes
     * listening to the publishing queue.
     *
     * @param message The message to publish.
     */
    @Override
    public void publish(Message<?> message) {
        try {
            // pack bytes
            byte[] bytes = packSendingMessage(message, Domain.PUBLISH);

            // publish with routing key
            rmqChannel.basicPublish(EXCHANGE_NAME, PUBLISH_QUEUE_NAME, C_BASIC_PROPERTIES, bytes);
        } catch (Exception e) {
            // rethrow error
            Throwables.sneakyThrow(e);
        }
    }

    @Override
    public RMQChannel newChannel(String remote, boolean aux) {
        RMQChannel channel = new RMQChannel(this, remote, aux);
        withChannel(channel);

        if (aux) {
            try {
                // if auxiliary, create and listen on that queue
                rmqChannel.queueDeclare(remote, false, false, false, null);
                rmqChannel.queueBind(remote, EXCHANGE_NAME, localName);

                rmqChannel.basicConsume(remote, true, (consumerTag, message) -> receiveRMQ(message, remote), consumerTag -> { });
            } catch (Exception e) {
                // rethrow error
                Throwables.sneakyThrow(e);
            }
        }

        return channel;
    }

    @Override
    public void send(Message<?> message, RMQChannel channel) {
        send(message, channel.remote);
    }

    //////////////////////////////////////

    @Override
    public String remote() {
        return null;
    }

    @Override
    public void send(Message<?> message) {
        publish(message);
    }

}
