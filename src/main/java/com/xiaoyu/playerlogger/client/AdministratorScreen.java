package com.xiaoyu.playerlogger.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class AdministratorScreen extends Screen {

    private List<Map<String, Object>> serverData;
    private List<Map<String, Object>> filteredData;

    private int currentPage = 0;

    private Button nextButton;
    private Button prevButton;
    private EditBox searchBox;

    public AdministratorScreen(Component title, String data) {
        super(title);

        Gson gson = new Gson();
        Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
        serverData = gson.fromJson(data, type);

        filteredData = new ArrayList<>(serverData);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;

        // 搜索框
        searchBox = new EditBox(this.font, centerX - 100, 20, 200, 20, Component.literal("Search"));
        this.addRenderableWidget(searchBox);

        // 上一页
        prevButton = Button.builder(Component.translatable("gui.playerlogger.prev"), b -> {
            if (currentPage > 0) currentPage--;
        }).bounds(centerX - 110, this.height - 40, 100, 20).build();

        // 下一页
        nextButton = Button.builder(Component.translatable("gui.playerlogger.next"), b -> {
            if (currentPage < filteredData.size() - 1) currentPage++;
        }).bounds(centerX + 10, this.height - 40, 100, 20).build();

        this.addRenderableWidget(prevButton);
        this.addRenderableWidget(nextButton);
    }

    @Override
    public void tick() {
        super.tick();

        // 搜索过滤
        String keyword = searchBox.getValue().toLowerCase();

        filteredData = serverData.stream()
                .filter(map -> map.values().stream()
                        .anyMatch(v -> String.valueOf(v).toLowerCase().contains(keyword)))
                .collect(Collectors.toList());

        // 防止越界
        if (currentPage >= filteredData.size()) {
            currentPage = Math.max(0, filteredData.size() - 1);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        super.render(graphics, mouseX, mouseY, partialTick);

        int y = 60;
        int lineHeight = 12;

        if (filteredData.isEmpty()) {
            graphics.drawString(this.font, "No Data", 20, y, 0xFFFFFF);
            return;
        }

        Map<String, Object> data = filteredData.get(currentPage);

        for (Map.Entry<String, Object> entry : data.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();

            String line;

            // 特殊处理坐标
            if (key.equals("x") && data.containsKey("y") && data.containsKey("z")) {
                line = Component.translatable("gui.playerlogger.pos").getString() + ": " + data.get("x") + ", " + data.get("y") + ", " + data.get("z");
                graphics.drawString(this.font, line, 20, y, 0x00FFAA);
                y += lineHeight;

                continue;
            }

            // 避免重复显示 y z
            if (key.equals("y") || key.equals("z")) continue;

            line = key + ": " + value;

            graphics.drawString(this.font, line, 20, y, 0xFFFFFF);
            y += lineHeight;
        }

        // 页码
        String pageInfo = (currentPage + 1) + " / " + filteredData.size();
        graphics.drawString(this.font, pageInfo, this.width / 2 - 20, this.height - 60, 0xFFFF00);
    }
}