package com.xiaoyu.playerlogger.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class Packet {
    public record Client_Packet(String TABLE_NAME) implements CustomPacketPayload {

        public static final Type<Client_Packet> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("playerlogger", "client_packet"));

        public static final StreamCodec<FriendlyByteBuf, Client_Packet> STREAM_CODEC =
                StreamCodec.of(
                        (buf, pkt) -> buf.writeUtf(pkt.TABLE_NAME),
                        buf -> new Client_Packet(buf.readUtf())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record MySQL_Data_Packet(String DATA) implements CustomPacketPayload {

        public static final Type<MySQL_Data_Packet> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("playerlogger", "mysql_packet"));

        public static final StreamCodec<FriendlyByteBuf, MySQL_Data_Packet> STREAM_CODEC =
                StreamCodec.of(
                        (buf, pkt) -> buf.writeUtf(pkt.DATA),
                        buf -> new MySQL_Data_Packet(buf.readUtf())
                );

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
