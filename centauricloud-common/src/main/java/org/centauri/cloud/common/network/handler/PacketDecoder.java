package org.centauri.cloud.common.network.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import org.centauri.cloud.common.network.PacketManager;
import org.centauri.cloud.common.network.packets.Packet;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() <= 0)
			return;

		Packet packet = null;
		byte packetId = in.readByte();

		if (packetId < 0) {
			in.clear();
			throw new DecoderException("WTF, why is the packet id lower than zero?!?! Id: " + packetId);
		}
		Class<? extends Packet> clazz = PacketManager.getInstance().getPacketClass(packetId);

		if (clazz != null)
			packet = clazz.newInstance();

		if (packet == null) {
			throw new DecoderException("Cannot find packet id: " + packetId);
		}

		packet.decode(in);
		out.add(packet);
	}
}
