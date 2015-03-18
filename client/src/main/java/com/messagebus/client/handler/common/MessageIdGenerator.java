package com.messagebus.client.handler.common;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.model.HandlerModel;
import com.messagebus.common.Constants;
import com.messagebus.common.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Random;

public class MessageIdGenerator extends AbstractHandler {

    private static final Log    logger = LogFactory.getLog(MessageIdGenerator.class);
    private static final Random random = new Random();

    @Override
    public void init(HandlerModel handlerModel) {
        super.init(handlerModel);
    }

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        IMessage[] msgs = context.getMessages();
        for (IMessage msg : msgs) {
            UUIDGenerator generator = new UUIDGenerator(random.nextInt(31), Constants.DEFAULT_DATACENTER_ID_FOR_UUID);
            logger.debug("message id is : " + generator.nextId());
            msg.getMessageHeader().setMessageId(generator.nextId());
        }

        chain.handle(context);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
