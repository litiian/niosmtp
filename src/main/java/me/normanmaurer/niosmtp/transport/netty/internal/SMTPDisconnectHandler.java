/**
* Licensed to niosmtp developers ('niosmtp') under one or more
* contributor license agreements. See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* niosmtp licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package me.normanmaurer.niosmtp.transport.netty.internal;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelStateHandlerAdapter;
import me.normanmaurer.niosmtp.SMTPDisconnectedException;
import me.normanmaurer.niosmtp.SMTPResponse;
import me.normanmaurer.niosmtp.core.SMTPClientFutureImpl;
import me.normanmaurer.niosmtp.delivery.SMTPDeliverySessionConstants;
import me.normanmaurer.niosmtp.transport.FutureResult;
import me.normanmaurer.niosmtp.transport.SMTPClientSession;

/**
 * {@link ChannelStateHandlerAdapter} implementation which will throw an {@link SMTPDisconnectedException}
 * if a connection was disconnected unexpectedly.
 *
 * @author Raman Gupta
 *
 */
public class SMTPDisconnectHandler extends ChannelStateHandlerAdapter {

    private final SMTPClientFutureImpl<FutureResult<SMTPResponse>> future;

    public SMTPDisconnectHandler(SMTPClientFutureImpl<FutureResult<SMTPResponse>> future){
        this.future = future;
    }

    private boolean activeTransaction() {
        final SMTPClientSession session = future.getSession();
        return session != null && (Boolean) session.getAttribute(SMTPDeliverySessionConstants.SMTP_TRANSACTION_ACTIVE_KEY);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(activeTransaction()) {
            throw new SMTPDisconnectedException("Connection closed during transaction.");
        }
        super.channelInactive(ctx);
    }

    @Override
    public void inboundBufferUpdated(ChannelHandlerContext ctx) throws Exception {
        ctx.fireInboundBufferUpdated();
    }
}
