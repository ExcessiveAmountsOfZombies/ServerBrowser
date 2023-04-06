package com.epherical.serverbrowser.client.list;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.screen.ServerBrowserScreen;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class ServerBrowserList extends ObjectSelectionList<ServerBrowserList.Entry> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5,
            (new ThreadFactoryBuilder()).setNameFormat("ServerBrowser Pinger #%d")
                    .setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).build());
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");

    private static final Component CANT_RESOLVE_TEXT = Component.translatable("multiplayer.status.cannot_resolve").withStyle(ChatFormatting.DARK_RED);
    private static final Component CANT_CONNECT_TEXT = Component.translatable("multiplayer.status.cannot_connect").withStyle(ChatFormatting.DARK_RED);
    private static final Component INCOMPATIBLE_TOOLTIP = Component.translatable("multiplayer.status.incompatible");
    private static final Component NO_CONNECTION_TOOLTIP = Component.translatable("multiplayer.status.no_connection");
    private static final Component PINGING_TOOLTIP = Component.translatable("multiplayer.status.pinging");


    private final ServerBrowserScreen screen;

    private final List<BrowsedEntry> entries = new ArrayList<>();

    public ServerBrowserList(ServerBrowserScreen screen, Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
        this.screen = screen;
    }

    public void refreshServers() {
        for (BrowsedEntry entry : entries) {
            entry.getServerData().pinged = false;
        }
    }

    public List<BrowsedEntry> getEntries() {
        return entries;
    }

    public void queryServers() {
        entries.clear();
        this.clearEntries();

        /*JsonArray array = new JsonArray();
        if (ServerBrowserScreen.servers != null) {
            array = ServerBrowserScreen.servers.getAsJsonArray();
        }

        for (JsonElement element : array) {
            JsonObject object = (JsonObject) element;
            BrowsedEntry browsedEntry = new BrowsedEntry(screen, object, minecraft);
            if (browsedEntry.isValid()) {
                entries.add(browsedEntry);
                this.addEntry(browsedEntry);
            }
        }*/
    }

    public void addEntries(JsonElement jsonElement, boolean toTop) {
        JsonArray array = new JsonArray();
        array = jsonElement.getAsJsonArray();
        for (JsonElement element : array) {
            JsonObject object = (JsonObject) element;
            BrowsedEntry browsedEntry;
            if (toTop) {
                browsedEntry = new BrowsedEntry(screen, object.get("name").getAsString(),
                        object.get("ip").getAsString(), object.get("port").getAsInt(), minecraft);
            } else {
                browsedEntry = new BrowsedEntry(screen, object, minecraft);
            }
            if (browsedEntry.isValid()) {
                entries.add(browsedEntry);
                if (toTop) {
                    this.addEntryToTop(browsedEntry);
                } else {
                    this.addEntry(browsedEntry);
                }
            }
        }
    }




    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 30;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
    }

    public class BrowsedEntry extends Entry {

        private final ServerBrowserScreen screen;

        private final String serverName;
        private final String ipAddress;
        private final int port;
        private final Component description;
        private final List<String> tags;
        private final int rank;
        private final int bgColor;

        private ResourceLocation iconLocation;
        @Nullable
        private String lastIconB64;
        @Nullable
        private DynamicTexture icon;


        private final Minecraft minecraft;
        private ServerData serverData;
        private long lastClickTime;

        private boolean valid = true;

        public BrowsedEntry(ServerBrowserScreen screen, String serverName, String ipAddress, int port, Minecraft minecraft) {
            this.screen = screen;
            this.serverName = serverName;
            this.ipAddress = ipAddress;
            this.port = port;
            this.description = Component.literal("Unknown server from 3rd party service.");
            this.rank = 0;
            this.bgColor = 0x2163bf;
            this.minecraft = minecraft;
            this.tags = new ArrayList<>();
            assignCommonData();
        }


        public BrowsedEntry(ServerBrowserScreen screen, JsonObject object, Minecraft minecraft) {
            this.screen = screen;
            this.serverName = object.get("serverName").getAsString();
            this.ipAddress = object.get("ipAddress").getAsString();
            this.port = object.get("port").getAsInt();

            String address = this.ipAddress.toLowerCase(Locale.ROOT);
            if (CommonClient.getInstance().getSettings().getBlacklistedServers().contains(address)) {
                this.valid = false;
            }

            this.description = Component.literal(object.get("description").getAsString());
            List<String> tags = new ArrayList<>();
            JsonArray array = object.getAsJsonArray("tags");
            for (JsonElement tag : array) {
                tags.add(tag.getAsString());
            }
            this.tags = tags;
            this.rank = object.get("rank").getAsInt();
            this.bgColor = object.get("backgroundColor").getAsInt();
            this.minecraft = minecraft;
            assignCommonData();
        }

        private void assignCommonData() {
            String ip;
            if (port != 0) {
                ip = this.ipAddress + ":" + port;
            } else {
                ip = this.ipAddress;
            }

            this.iconLocation = new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(ip) + "/icon");
            AbstractTexture abstractTexture = minecraft.getTextureManager().getTexture(this.iconLocation, MissingTextureAtlasSprite.getTexture());
            if (abstractTexture != MissingTextureAtlasSprite.getTexture() && abstractTexture instanceof DynamicTexture) {
                this.icon = (DynamicTexture) abstractTexture;
            }
            this.serverData = new ServerData(serverName, ipAddress, false);
        }


        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            double d = mouseX - (double) ServerBrowserList.this.getRowLeft();
            if (d <= 32.0) {
                if (d < 32.0 && d > 16.0 && this.canJoin()) {
                    this.screen.setSelected(this);
                    this.screen.joinSelectedServer();
                    return true;
                }
            }

            this.screen.setSelected(this);
            if (Util.getMillis() - this.lastClickTime < 250L) {
                this.screen.joinSelectedServer();
            }

            this.lastClickTime = Util.getMillis();
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal("howdy partner");
        }

        @Override
        public void render(PoseStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            if (!this.serverData.pinged) {
                this.serverData.pinged = true;
                this.serverData.ping = -2L;
                this.serverData.motd = CommonComponents.EMPTY;
                this.serverData.status = CommonComponents.EMPTY;
                THREAD_POOL.submit(() -> {
                    try {
                        this.screen.getPinger().pingServer(this.serverData, () -> {
                            this.minecraft.execute(() -> {
                            });
                        });
                    } catch (UnknownHostException var2) {
                        this.serverData.ping = -1L;
                        this.serverData.motd = CANT_RESOLVE_TEXT;
                    } catch (Exception var3) {
                        this.serverData.ping = -1L;
                        this.serverData.motd = CANT_CONNECT_TEXT;
                    }

                });
            }

            boolean bl = this.serverData.protocol != SharedConstants.getCurrentVersion().getProtocolVersion();
            this.minecraft.font.draw(poseStack, this.serverData.name, (float) (left + 32 + 3), (float) (top + 1), 16777215);
            List<FormattedCharSequence> list = this.minecraft.font.split(this.serverData.motd, width - 32 - 2);

            for (int i = 0; i < Math.min(list.size(), 2); ++i) {
                this.minecraft.font.draw(poseStack, list.get(i), (left + 32 + 3), (float) (top + 12 + 9 * i), 8421504);
            }

            Component component = bl ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status;
            int textWidth = this.minecraft.font.width(component);
            this.minecraft.font.draw(poseStack, component, (float) (left + width - textWidth - 15 - 2), (float) (top + 1), 8421504);
            int k = 0;
            int latency;
            List<Component> list2;
            Component component2;
            if (bl) {
                latency = 5;
                component2 = INCOMPATIBLE_TOOLTIP;
                list2 = this.serverData.playerList;
            } else if (this.serverData.pinged && this.serverData.ping != -2L) {
                if (this.serverData.ping < 0L) {
                    latency = 5;
                } else if (this.serverData.ping < 150L) {
                    latency = 0;
                } else if (this.serverData.ping < 300L) {
                    latency = 1;
                } else if (this.serverData.ping < 600L) {
                    latency = 2;
                } else if (this.serverData.ping < 1000L) {
                    latency = 3;
                } else {
                    latency = 4;
                }

                if (this.serverData.ping < 0L) {
                    component2 = NO_CONNECTION_TOOLTIP;
                    list2 = Collections.emptyList();
                } else {
                    component2 = Component.translatable("multiplayer.status.ping", this.serverData.ping);
                    list2 = this.serverData.playerList;
                }
            } else {
                k = 1;
                latency = (int) (Util.getMillis() / 100L + (index * 2L) & 7L);
                if (latency > 4) {
                    latency = 8 - latency;
                }

                component2 = PINGING_TOOLTIP;
                list2 = Collections.emptyList();
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            GuiComponent.blit(poseStack, left + width - 15, top, (float) (k * 10), (float) (176 + latency * 8), 10, 8, 256, 256);
            String string = this.serverData.getIconB64();
            if (!Objects.equals(string, this.lastIconB64)) {
                if (this.uploadServerIcon(string)) {
                    this.lastIconB64 = string;
                } else {
                    this.serverData.setIconB64(null);
                    //this.updateServerList();
                }
            }

            if (this.icon == null) {
                this.drawIcon(poseStack, left, top, ICON_MISSING);
            } else {
                this.drawIcon(poseStack, left, top, this.iconLocation);
            }

            int m = mouseX - left;
            int n = mouseY - top;

            if (this.minecraft.options.touchscreen().get() || isMouseOver) {
                RenderSystem.setShaderTexture(0, ICON_OVERLAY_LOCATION);
                GuiComponent.fill(poseStack, left, top, left + 32, top + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int o = mouseX - left;
                if (this.canJoin()) {
                    int currentLength = 0;
                    int maxLength = 150;
                    List<String> condensedTags = new ArrayList<>();
                    condensedTags.add("Tags");
                    StringBuilder condenser = new StringBuilder();
                    tags.sort(String::compareTo);
                    for (String tag : tags) {
                        if (currentLength >= maxLength) {
                            currentLength = 0;
                            condensedTags.add(condenser.append(",").toString());
                            condenser = new StringBuilder();
                        } else {
                            if (condenser.length() == 0) {
                                condenser.append(tag);
                            } else {
                                condenser.append(", ").append(tag);
                            }
                        }
                        currentLength += minecraft.font.width(tag);
                    }
                    condensedTags.add(condenser.toString());
                    List<Component> components = condensedTags.stream().map(Component::literal).collect(Collectors.toList());
                    screen.setToolTip(components);
                    // TODO; background hovering here
                    /*if (o < 32 && o > 16) {
                        GuiComponent.blit(poseStack, left, top, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(poseStack, left, top, 0.0F, 0.0F, 32, 32, 256, 256);
                    }*/
                }
            }

            if (m >= width - 15 && m <= width - 5 && n >= 0 && n <= 8) {
                this.screen.setToolTip(Collections.singletonList(component2));
            } else if (m >= width - textWidth - 15 - 2 && m <= width - 15 - 2 && n >= 0 && n <= 8) {
                this.screen.setToolTip(list2);
            }

        }

        public boolean isValid() {
            return valid;
        }

        protected void drawIcon(PoseStack poseStack, int x, int y, ResourceLocation textureLocation) {
            RenderSystem.setShaderTexture(0, textureLocation);
            RenderSystem.enableBlend();
            GuiComponent.blit(poseStack, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
        }

        private boolean uploadServerIcon(@Nullable String icon) {
            if (icon == null) {
                this.minecraft.getTextureManager().release(this.iconLocation);
                if (this.icon != null && this.icon.getPixels() != null) {
                    this.icon.getPixels().close();
                }

                this.icon = null;
            } else {
                try {
                    NativeImage nativeImage = NativeImage.fromBase64(icon);
                    Validate.validState(nativeImage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(nativeImage.getHeight() == 64, "Must be 64 pixels high");
                    if (this.icon == null) {
                        this.icon = new DynamicTexture(nativeImage);
                    } else {
                        this.icon.setPixels(nativeImage);
                        this.icon.upload();
                    }

                    this.minecraft.getTextureManager().register(this.iconLocation, this.icon);
                } catch (Throwable var3) {
                    LOGGER.error("Invalid icon for server {} ({})", this.serverData.name, this.serverData.ip, var3);
                    return false;
                }
            }

            return true;
        }

        private boolean canJoin() {
            return true;
        }

        public ServerData getServerData() {
            return serverData;
        }
    }
}
