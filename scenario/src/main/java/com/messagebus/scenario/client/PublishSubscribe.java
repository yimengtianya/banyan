package com.messagebus.scenario.client;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 2/24/15.
 */
public class PublishSubscribe {

    private static final Log logger = LogFactory.getLog(PublishSubscribe.class);

    private static final String host = "127.0.0.1";
    private static final int    port = 6379;

    public static void main(String[] args) {
        publish();

        subscribe1();

        subscribe2();
    }

    private static void publish() {
        String secret = "oiqwenncuicnsdfuasdfnkajkwqowe";
        String token = "kjkjasdjfhkajsdfhksdjhfkasdf";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        IMessage msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        Message.MessageBody body = new Message.MessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        client.publish(secret, new IMessage[]{msg}, token);

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    private static void subscribe1() {
        String secret = "nckljsenlkjanefluiwnlanfmsdfas";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        client.subscribe(secret, new IMessageReceiveListener() {
            @Override
            public void onMessage(IMessage message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        }, 3, TimeUnit.SECONDS);

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    private static void subscribe2() {
        String secret = "zxcnvblawelkusahdfqwiuhowefhnx";

        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        client.subscribe(secret, new IMessageReceiveListener() {
            @Override
            public void onMessage(IMessage message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        }, 3, TimeUnit.SECONDS);

        singlePool.returnResource(client);
        singlePool.destroy();
    }

}
