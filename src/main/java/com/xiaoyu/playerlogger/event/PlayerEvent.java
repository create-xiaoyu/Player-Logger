package com.xiaoyu.playerlogger.event;

import com.xiaoyu.playerlogger.PlayerLogger;
import com.xiaoyu.playerlogger.database.DatabaseExecutor;
import com.xiaoyu.playerlogger.database.MySQLUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.DEDICATED_SERVER)
public class PlayerEvent {
    private BlockState nowPlayerRightClickBlockStack;
    private BlockPos nowPlayerRightClicBlockPos;

    @SubscribeEvent
    public void onPlayerLogging(PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String playerUuid =  player.getStringUUID();
        String playerName =  player.getName().getString();
        String playerIp =  player.getIpAddress();

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        String playerDimension = player.level().dimension().location().toString();

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        DatabaseExecutor.getExecutor().execute(() -> {
            try (Connection connect = MySQLUtils.getMySQLConnect()) {
                String sql = "INSERT INTO player_info (uuid, name, ip) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), ip = VALUES(ip)";

                try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                    pstmt.setString(1, playerUuid);
                    pstmt.setString(2, playerName);
                    pstmt.setString(3, playerIp);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    PlayerLogger.LOGGER.error("Insert player {} ({}) failure!", playerName, playerUuid, e);
                }

                sql = "INSERT INTO player_data_login (uuid, dimension, x, y, z, time) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE dimension = VALUES(dimension), x = VALUES(x), y = VALUES(y), z = VALUES(z), time = VALUES(time)";

                try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                    pstmt.setString(1, playerUuid);
                    pstmt.setString(2, playerDimension);
                    pstmt.setDouble(3, playerX);
                    pstmt.setDouble(4, playerY);
                    pstmt.setDouble(5, playerZ);
                    pstmt.setString(6, time);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    PlayerLogger.LOGGER.error("Insert player player_data_login failure!", e);
                }
            } catch (SQLException e) {
                PlayerLogger.LOGGER.error("Connect MySQL Server Failure!", e);
            }
        });
    }

    @SubscribeEvent
    public void onPlayerOutLogging(PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String playerUuid =  player.getStringUUID();

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();

        String playerDimension = player.level().dimension().location().toString();

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        DatabaseExecutor.getExecutor().execute(() -> {
            try (Connection connect = MySQLUtils.getMySQLConnect()) {
                String sql = "INSERT INTO player_data_logout (uuid, dimension, x, y, z, time) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE dimension = VALUES(dimension), x = VALUES(x), y = VALUES(y), z = VALUES(z), time = VALUES(time)";

                try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                    pstmt.setString(1, playerUuid);
                    pstmt.setString(2, playerDimension);
                    pstmt.setDouble(3, playerX);
                    pstmt.setDouble(4, playerY);
                    pstmt.setDouble(5, playerZ);
                    pstmt.setString(6, time);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    PlayerLogger.LOGGER.error("Insert player player_data_logout failure!", e);
                }
            } catch (SQLException e) {
                PlayerLogger.LOGGER.error("Connect MySQL Server Failure!", e);
            }
        });
    }

    @SubscribeEvent
    public void onBlockEvent$BreakEvent(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        String playerUuid =  player.getStringUUID();

        double blockX = event.getPos().getX();
        double blockY = event.getPos().getY();
        double blockZ = event.getPos().getZ();

        String playerDimension = player.level().dimension().location().toString();

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());

        String ID = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString();
        String NBT;

        if (blockEntity != null) {
            NBT = blockEntity.saveWithFullMetadata(event.getLevel().registryAccess()).toString();
        } else {
            NBT = "null";
        }

        DatabaseExecutor.getExecutor().execute(() -> {
            try (Connection connect = MySQLUtils.getMySQLConnect()) {
                String sql = "INSERT INTO player_break_block (uuid, dimension, x, y, z, blockID, blockNBT, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                    pstmt.setString(1, playerUuid);
                    pstmt.setString(2, playerDimension);
                    pstmt.setDouble(3, blockX);
                    pstmt.setDouble(4, blockY);
                    pstmt.setDouble(5, blockZ);
                    pstmt.setString(6, ID);
                    pstmt.setString(7, NBT);
                    pstmt.setString(8, time);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    PlayerLogger.LOGGER.error("Insert player player_break_block failure!", e);
                }
            } catch (SQLException e) {
                PlayerLogger.LOGGER.error("Connect MySQL Server Failure!", e);
            }
        });
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        nowPlayerRightClickBlockStack = event.getLevel().getBlockState(event.getPos());
        nowPlayerRightClicBlockPos = event.getPos();
    }

    @SubscribeEvent
    public void onPlayerOpenContainer(PlayerContainerEvent.Open event) {
        // 强制转换为 ServerPlayer 对象
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // 定义基础变量
        String playerUuid =  player.getStringUUID();
        String playerDimension = player.level().dimension().location().toString();
        Integer blockX = nowPlayerRightClicBlockPos.getX();
        Integer blockY = nowPlayerRightClicBlockPos.getY();
        Integer blockZ = nowPlayerRightClicBlockPos.getZ();
        String contatinerID = BuiltInRegistries.BLOCK.getKey(nowPlayerRightClickBlockStack.getBlock()).toString();
        final String[] modifyType = new String[1];

        // 获取容器对象
        AbstractContainerMenu container = event.getContainer();

        // 容器格子范围
        int slotSize = container.slots.size() - 36;

        // 旧数据缓存
        Map<Integer, Map<String, String>> oldContainerData = new HashMap<>();

        // 遍历容器每个格子
        for (Slot slot : container.slots) {
            if (slot.index < slotSize) {

                // 将数据缓存到 oldContainerData
                Map<String, String> oldData = new HashMap<>();
                oldData.put("item", BuiltInRegistries.ITEM.getKey(slot.getItem().getItem()).toString());
                oldData.put("count", String.valueOf(slot.getItem().getCount()));

                oldContainerData.put(slot.index, oldData);
            }
        }

        // 新建监听器，监听格子改变
        container.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu abstractContainerMenu, int i, ItemStack itemStack) {
                if (i < slotSize) {
                    // 获取旧数据
                    Map<String, String > oldContainerDataMap = oldContainerData.get(i);

                    // 如果新数据的物品与数量和旧数据相同则判断为没有更改，直接返回
                    if (oldContainerDataMap.get("item").equals(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString()) && oldContainerDataMap.get("count").equals(String.valueOf(itemStack.getCount()))) return;

                    // 如果新旧数据物品一样，判断物品数量变化
                    if (oldContainerDataMap.get("item").equals(BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString())) {
                        // 如果旧数据的物品数量大于新数据的物品数量则判断操作为取出，反之存入
                        if (Integer.parseInt(oldContainerDataMap.get("count")) > itemStack.getCount()) {
                            modifyType[0] = "Take";
                        } else {
                            modifyType[0] = "Deposit";
                        }
                        // 如果旧数据是空气，则判断这个地方被存入了物品
                    } else if (oldContainerDataMap.get("item").equals("minecraft:air")) {
                        modifyType[0] = "Deposit";
                        // 如果新数据是空气，则判断这个地方被取出了物品
                    } else  if (BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString().equals("minecraft:air")) {
                        modifyType[0] = "Take";
                    } else {
                        modifyType[0] = "Modify";
                    }

                    // 时间
                    String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    // 异步写入数据库
                    DatabaseExecutor.getExecutor().execute(() -> {
                        try (Connection connect = MySQLUtils.getMySQLConnect()) {
                            String sql = "INSERT INTO player_modify_container (uuid, dimension, x, y, z, containerID, type, oldItem, newItem, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                            try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                                pstmt.setString(1, playerUuid);
                                pstmt.setString(2, playerDimension);
                                pstmt.setInt(3, blockX);
                                pstmt.setInt(4, blockY);
                                pstmt.setInt(5, blockZ);
                                pstmt.setString(6, contatinerID);
                                pstmt.setString(7, modifyType[0]);
                                pstmt.setString(8, oldContainerDataMap.get("item") + " *" + oldContainerDataMap.get("count"));
                                pstmt.setString(9, BuiltInRegistries.ITEM.getKey(itemStack.getItem()) + " *" + itemStack.getCount());
                                pstmt.setString(10, time);
                                pstmt.executeUpdate();
                            } catch (SQLException e) {
                                PlayerLogger.LOGGER.error("Insert player player_modify_container failure!", e);
                            }
                        } catch (SQLException e) {
                            PlayerLogger.LOGGER.error("Connect MySQL Server Failure!", e);
                        }
                    });

                    // 更新旧数据缓存
                    Map<String, String> newData = new HashMap<>();
                    newData.put("item", BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString());
                    newData.put("count", String.valueOf(itemStack.getCount()));

                    oldContainerData.put(i, newData);
                }
            }

            @Override
            public void dataChanged(AbstractContainerMenu abstractContainerMenu, int i, int i1) {

            }
        });
    }

    @SubscribeEvent
    public void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        String playerUuid =  player.getStringUUID();

        double blockX = event.getPos().getX();
        double blockY = event.getPos().getY();
        double blockZ = event.getPos().getZ();

        String playerDimension = player.level().dimension().location().toString();

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());

        String ID = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock()).toString();
        String NBT;

        if (blockEntity != null) {
            NBT = blockEntity.saveWithFullMetadata(event.getLevel().registryAccess()).toString();
        } else {
            NBT = "null";
        }

        DatabaseExecutor.getExecutor().execute(() -> {
            try (Connection connect = MySQLUtils.getMySQLConnect()) {
                String sql = "INSERT INTO player_place_block (uuid, dimension, x, y, z, blockID, blockNBT, time) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = connect.prepareStatement(sql)) {
                    pstmt.setString(1, playerUuid);
                    pstmt.setString(2, playerDimension);
                    pstmt.setDouble(3, blockX);
                    pstmt.setDouble(4, blockY);
                    pstmt.setDouble(5, blockZ);
                    pstmt.setString(6, ID);
                    pstmt.setString(7, NBT);
                    pstmt.setString(8, time);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    PlayerLogger.LOGGER.error("Insert player player_place_block failure!", e);
                }
            } catch (SQLException e) {
                PlayerLogger.LOGGER.error("Connect MySQL Server Failure!", e);
            }
        });
    }
}
