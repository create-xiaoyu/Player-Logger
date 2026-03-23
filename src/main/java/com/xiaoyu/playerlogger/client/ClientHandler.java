package com.xiaoyu.playerlogger.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientHandler {
    public static void handleMySQLData(String data) {
        Minecraft.getInstance().setScreen(
                new AdministratorScreen(Component.literal("Administrator GUI"), data)
        );
    }
}
