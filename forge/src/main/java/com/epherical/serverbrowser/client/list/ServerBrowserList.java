package com.epherical.serverbrowser.client.list;

import com.epherical.serverbrowser.client.CommonClient;
import com.epherical.serverbrowser.client.screen.ServerBrowserScreen;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class ServerBrowserList extends ExtendedList<ServerBrowserList.Entry> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5,
            (new ThreadFactoryBuilder()).setNameFormat("ServerBrowser Pinger #%d")
                    .setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).build());
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/server_selection.png");

    private static final ITextComponent CANT_RESOLVE_TEXT = new TranslationTextComponent("multiplayer.status.cannot_resolve").withStyle(TextFormatting.DARK_RED);
    private static final ITextComponent CANT_CONNECT_TEXT = new TranslationTextComponent("multiplayer.status.cannot_connect").withStyle(TextFormatting.DARK_RED);
    private static final ITextComponent INCOMPATIBLE_TOOLTIP = new TranslationTextComponent("multiplayer.status.incompatible");
    private static final ITextComponent NO_CONNECTION_TOOLTIP = new TranslationTextComponent("multiplayer.status.no_connection");
    private static final ITextComponent PINGING_TOOLTIP = new TranslationTextComponent("multiplayer.status.pinging");


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

        JsonArray array = new JsonArray();
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
        }
    }


    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 30;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    public abstract static class Entry extends ExtendedList.AbstractListEntry<Entry> {
    }

    public class BrowsedEntry extends Entry {

        private final ServerBrowserScreen screen;

        private final String serverName;
        private final String ipAddress;
        private final int port;
        private final ITextComponent description;
        private final List<String> tags;
        private final int rank;
        private final int bgColor;

        private final ResourceLocation iconLocation;
        @Nullable
        private String lastIconB64;
        @Nullable
        private DynamicTexture icon;


        private final Minecraft minecraft;
        private final ServerData serverData;
        private long lastClickTime;

        private boolean valid = true;


        public BrowsedEntry(ServerBrowserScreen screen, JsonObject object, Minecraft minecraft) {
            this.screen = screen;
            this.serverName = object.get("serverName").getAsString();
            this.ipAddress = object.get("ipAddress").getAsString();
            this.port = object.get("port").getAsInt();

            String address = this.ipAddress.toLowerCase(Locale.ROOT);
            if (CommonClient.getInstance().getSettings().getBlacklistedServers().contains(address)) {
                this.valid = false;
            }

            this.description = new StringTextComponent(object.get("description").getAsString());
            List<String> tags = new ArrayList<>();
            JsonArray array = object.getAsJsonArray("tags");
            for (JsonElement tag : array) {
                tags.add(tag.getAsString());
            }
            this.tags = tags;
            this.rank = object.get("rank").getAsInt();
            this.bgColor = object.get("backgroundColor").getAsInt();
            this.minecraft = minecraft;

            String ip;
            if (port != 0) {
                ip = this.ipAddress + ":" + port;
            } else {
                ip = this.ipAddress;
            }

            this.iconLocation = new ResourceLocation("servers/" + Hashing.sha1().hashUnencodedChars(ip) + "/icon");
            this.icon = (DynamicTexture) this.minecraft.getTextureManager().getTexture(this.iconLocation);
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
        public void render(MatrixStack poseStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            if (!this.serverData.pinged) {
                this.serverData.pinged = true;
                this.serverData.ping = -2L;
                this.serverData.motd = StringTextComponent.EMPTY;
                this.serverData.status = StringTextComponent.EMPTY;
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
            List<IReorderingProcessor> list = this.minecraft.font.split(this.serverData.motd, width - 32 - 2);

            for (int i = 0; i < Math.min(list.size(), 2); ++i) {
                this.minecraft.font.draw(poseStack, list.get(i), (left + 32 + 3), (float) (top + 12 + 9 * i), 8421504);
            }

            ITextComponent ITextComponent = bl ? this.serverData.version.copy().withStyle(TextFormatting.RED) : this.serverData.status;
            int textWidth = this.minecraft.font.width(ITextComponent);
            this.minecraft.font.draw(poseStack, ITextComponent, (float) (left + width - textWidth - 15 - 2), (float) (top + 1), 8421504);
            int k = 0;
            int latency;
            List<ITextComponent> list2;
            ITextComponent component2;
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
                    component2 = new TranslationTextComponent("multiplayer.status.ping", this.serverData.ping);
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

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
            AbstractGui.blit(poseStack, left + width - 15, top, (float) (k * 10), (float) (176 + latency * 8), 10, 8, 256, 256);
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

            if (this.minecraft.options.touchscreen || isMouseOver) {
                this.minecraft.getTextureManager().bind(ICON_OVERLAY_LOCATION);
                AbstractGui.fill(poseStack, left, top, left + 32, top + 32, -1601138544);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
                    List<ITextComponent> components = condensedTags.stream().map(StringTextComponent::new).collect(Collectors.toList());
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

        protected void drawIcon(MatrixStack poseStack, int x, int y, ResourceLocation textureLocation) {
            this.minecraft.getTextureManager().bind(textureLocation);
            RenderSystem.enableBlend();
            AbstractGui.blit(poseStack, x, y, 0.0F, 0.0F, 32, 32, 32, 32);
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
