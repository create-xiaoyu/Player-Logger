package com.xiaoyu.playerlogger.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.xiaoyu.playerlogger.network.Packet;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

@OnlyIn(Dist.CLIENT)
public class Command {
    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("playerlogger")
                        .requires(
                                c -> c.hasPermission(4)
                        )
                        .then(
                                Commands.argument("table", StringArgumentType.string())
                                        .suggests((ctx, builder) -> builder
                                                .suggest("player_info")
                                                .suggest("player_data_login")
                                                .suggest("player_data_logout")
                                                .suggest("player_break_block")
                                                .suggest("player_modify_container")
                                                .suggest("player_place_block")
                                                .buildFuture())
                                        .executes(context -> {
                                            String table = context.getArgument("table", String.class);
                                            Packet.Client_Packet packet = new Packet.Client_Packet(table);

                                            Minecraft.getInstance().getConnection().send(packet);
                                            return 1;
                                        })
                        )

        );
    }
}
