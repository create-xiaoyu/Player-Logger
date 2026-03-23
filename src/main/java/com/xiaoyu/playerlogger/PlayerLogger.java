package com.xiaoyu.playerlogger;

import com.google.gson.Gson;
import com.mojang.logging.LogUtils;

import com.xiaoyu.playerlogger.client.ClientHandler;
import com.xiaoyu.playerlogger.commands.Command;
import com.xiaoyu.playerlogger.database.DatabaseExecutor;
import com.xiaoyu.playerlogger.database.MySQLUtils;
import com.xiaoyu.playerlogger.event.PlayerEvent;
import com.xiaoyu.playerlogger.network.Packet;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Mod(PlayerLogger.MODID)
public class PlayerLogger {

    public static final String MODID = "playerlogger";

    public static final Logger LOGGER = LogUtils.getLogger();

    public PlayerLogger(ModContainer modContainer, IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            NeoForge.EVENT_BUS.register(new PlayerEvent());
        }
        NeoForge.EVENT_BUS.addListener(this::onServerStop);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        modEventBus.addListener(this::onRegisterPayload);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public void onRegisterCommands(RegisterClientCommandsEvent event) {
        Command.registerCommands(event);
    }

    public void onRegisterPayload(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                Packet.Client_Packet.TYPE,
                Packet.Client_Packet.STREAM_CODEC,
                (packet, context) -> DatabaseExecutor.getExecutor().execute(() -> {
                    try (Connection connect = MySQLUtils.getMySQLConnect()) {
                        Gson gson = new Gson();
                        String sqlData = gson.toJson(MySQLUtils.searchAllTableData(connect, packet.TABLE_NAME()));
                        context.enqueueWork(() -> context.reply(new Packet.MySQL_Data_Packet(sqlData)));
                    } catch (SQLException e) {
                        PlayerLogger.LOGGER.error("Connect MySQL Server Failure!", e);
                    }
                })
        );

        registrar.playToClient(
                Packet.MySQL_Data_Packet.TYPE,
                Packet.MySQL_Data_Packet.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() -> ClientHandler.handleMySQLData(packet.DATA()))
        );
    }

    public void onServerStarting(ServerStartingEvent event) {
        if (!(event.getServer().isDedicatedServer())) return;

        DatabaseExecutor.init(Config.THREAD_NUMBER.get());
        MySQLUtils.initMySQLConnect();

        try (Connection connect = MySQLUtils.getMySQLConnect()) {
            try (Statement stmt = connect.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS player_info (" +
                        "uuid VARCHAR(50) PRIMARY KEY, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "ip VARCHAR(50) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                stmt.execute("CREATE TABLE IF NOT EXISTS player_data_login (" +
                        "uuid VARCHAR(50) PRIMARY KEY, " +
                        "dimension VARCHAR(50) NOT NULL," +
                        "x DOUBLE NOT NULL," +
                        "y DOUBLE NOT NULL," +
                        "z DOUBLE NOT NULL," +
                        "time VARCHAR(50) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                stmt.execute("CREATE TABLE IF NOT EXISTS player_data_logout (" +
                        "uuid VARCHAR(50) PRIMARY KEY, " +
                        "dimension VARCHAR(50) NOT NULL," +
                        "x DOUBLE NOT NULL," +
                        "y DOUBLE NOT NULL," +
                        "z DOUBLE NOT NULL," +
                        "time VARCHAR(50) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                stmt.execute("CREATE TABLE IF NOT EXISTS player_break_block (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "uuid VARCHAR(50) NOT NULL, " +
                        "dimension VARCHAR(50) NOT NULL," +
                        "x DOUBLE NOT NULL," +
                        "y DOUBLE NOT NULL," +
                        "z DOUBLE NOT NULL," +
                        "blockID VARCHAR(50) NOT NULL," +
                        "blockNBT MEDIUMTEXT, " +
                        "time VARCHAR(50) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                stmt.execute("CREATE TABLE IF NOT EXISTS player_modify_container (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "uuid VARCHAR(50) NOT NULL, " +
                        "dimension VARCHAR(50) NOT NULL," +
                        "x INT NOT NULL," +
                        "y INT NOT NULL," +
                        "z INT NOT NULL," +
                        "containerID VARCHAR(50) NOT NULL," +
                        "type VARCHAR(50) NOT NULL, " +
                        "oldItem VARCHAR(50) NOT NULL, " +
                        "newItem VARCHAR(50) NOT NULL, " +
                        "time VARCHAR(50) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                stmt.execute("CREATE TABLE IF NOT EXISTS player_place_block (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "uuid VARCHAR(50) NOT NULL, " +
                        "dimension VARCHAR(50) NOT NULL," +
                        "x DOUBLE NOT NULL," +
                        "y DOUBLE NOT NULL," +
                        "z DOUBLE NOT NULL," +
                        "blockID VARCHAR(50) NOT NULL," +
                        "blockNBT MEDIUMTEXT, " +
                        "time VARCHAR(50) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
                PlayerLogger.LOGGER.info("Init database completed");
            } catch (SQLException e) {
                PlayerLogger.LOGGER.error("Init table failure!", e);
            }
        } catch (SQLException e) {
            PlayerLogger.LOGGER.error("Connect MySQL Server Failure!", e);
        }
    }

    public void onServerStop(ServerStoppingEvent event) {
        if (!(event.getServer().isDedicatedServer())) return;

        DatabaseExecutor.shutdown();
        MySQLUtils.shutdownConnect();
    }
}
