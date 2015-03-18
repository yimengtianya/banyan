package com.messagebus.benchmark.client.testCase;

import com.messagebus.benchmark.client.Benchmark;
import com.messagebus.benchmark.client.IFetcher;
import com.messagebus.benchmark.client.ITerminater;
import com.messagebus.benchmark.client.TestConfigConstant;
import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.message.transfer.MsgBodyTransfer;
import com.messagebus.interactor.proxy.ProxyConsumer;
import com.messagebus.interactor.rabbitmq.AbstractInitializer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class OriginalConsumeTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(OriginalConsumeTestCase.class);

    public static class BasicConsume extends AbstractInitializer implements Runnable, ITerminater, IFetcher {

        private static final String  consumerTag   = "tag.consumer.msgLog";
        private              boolean flag          = true;
        private              long    counter       = 0;
        private              String  realQueueName = "";

        public BasicConsume(String host) {
            super(host);
        }

        @Override
        public long fetch() {
            return this.counter;
        }

        @Override
        public void terminate() {
            this.flag = false;
        }

        @Override
        public void run() {
            try {
                super.init();
                QueueingConsumer consumer = ProxyConsumer.consume(this.channel,
                                                                  this.getRealQueueName(),
                                                                  consumerTag);

                while (flag) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    AMQP.BasicProperties properties = delivery.getProperties();
                    byte[] msgBody = delivery.getBody();

                    this.channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                    String msgTypeStr = properties.getType();
                    if (msgTypeStr == null || msgTypeStr.isEmpty()) {
                        logger.error("[run] message type is null or empty");
                    }

                    MessageType msgType = null;
                    try {
                        msgType = MessageType.lookup(msgTypeStr);
                    } catch (UnknownError unknownError) {
                        throw new RuntimeException("unknown message type : " + msgTypeStr);
                    }

                    IMessage msg = MessageFactory.createMessage(msgType);
                    initMessage(msg, msgType, properties, msgBody);

                    ++counter;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    this.channel.basicCancel(consumerTag);
                    super.close();
                } catch (IOException e) {
                    logger.error("[run] occurs a IOException : " + e.getMessage());
                }
            }
        }

        private void initMessage(IMessage msg, MessageType msgType, AMQP.BasicProperties properties, byte[] bodyData) {
            MessageHeaderTransfer.unbox(properties, msgType, msg.getMessageHeader());
            msg.setMessageBody(MsgBodyTransfer.unbox(bodyData));
        }

        public String getRealQueueName() {
            return realQueueName;
        }

        public void setRealQueueName(String realQueueName) {
            this.realQueueName = realQueueName;
        }
    }

    public static void main(String[] args) {
        //produce some message for consuming
//        TestUtility.produce(50_0000);
//
//        try {
//            TimeUnit.SECONDS.sleep(30);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        String host = "172.16.206.30";

        OriginalConsumeTestCase testCase = new OriginalConsumeTestCase();
        BasicConsume task = new BasicConsume(host);
        task.setRealQueueName("queue.proxy.message.business.crm");

        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS, TestConfigConstant.FETCH_NUM,
                      "single_thread_original_consume_async_" + TestConfigConstant.MSG_BODY_SIZE_OF_KB + "_KB");
    }

}
