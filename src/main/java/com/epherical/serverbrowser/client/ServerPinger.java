package com.epherical.serverbrowser.client;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ServerPinger {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component CANT_CONNECT_MESSAGE = new TranslatableComponent("multiplayer.status.cannot_connect").withStyle((style) -> style.withColor(-65536));

    private final List<Connection> connections = Collections.synchronizedList(Lists.newArrayList());

    public void pingServer(final ServerData data, final Runnable runnable) throws UnknownHostException {
        ServerAddress serverAddress = ServerAddress.parseString(data.ip);
        Optional<InetSocketAddress> optionalINet = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
        if (!optionalINet.isPresent()) {
            this.onPingFailed(ConnectScreen.UNKNOWN_HOST_MESSAGE, data);
        } else {
            final InetSocketAddress socketAddress = optionalINet.get();
            final Connection connection = Connection.connectToServer(socketAddress, false);
            this.connections.add(connection);
            data.motd = new TranslatableComponent("multiplayer.status.pinging");
            data.ping = -1L;
            data.playerList = Collections.emptyList();
            connection.setListener(new ClientStatusPacketListener() {
                private boolean success;
                private boolean receivedPing;
                private long pingStart;

                public void handleStatusResponse(ClientboundStatusResponsePacket responsePacket) {
                    if (this.receivedPing) {
                        connection.disconnect(new TranslatableComponent("multiplayer.status.unrequested"));
                    } else {
                        this.receivedPing = true;
                        ServerStatus serverstatus = responsePacket.getStatus();
                        if (serverstatus.getDescription() != null) {
                            data.motd = serverstatus.getDescription();
                        } else {
                            data.motd = TextComponent.EMPTY;
                        }

                        if (serverstatus.getVersion() != null) {
                            data.version = new TextComponent(serverstatus.getVersion().getName());
                            data.protocol = serverstatus.getVersion().getProtocol();
                        } else {
                            data.version = new TranslatableComponent("multiplayer.status.old");
                            data.protocol = 0;
                        }

                        if (serverstatus.getPlayers() != null) {
                            data.status = ServerPinger.formatPlayerCount(serverstatus.getPlayers().getNumPlayers(), serverstatus.getPlayers().getMaxPlayers());
                            List<Component> list = Lists.newArrayList();
                            GameProfile[] agameprofile = serverstatus.getPlayers().getSample();
                            if (agameprofile != null && agameprofile.length > 0) {
                                for(GameProfile gameprofile : agameprofile) {
                                    list.add(new TextComponent(gameprofile.getName()));
                                }

                                if (agameprofile.length < serverstatus.getPlayers().getNumPlayers()) {
                                    list.add(new TranslatableComponent("multiplayer.status.and_more", serverstatus.getPlayers().getNumPlayers() - agameprofile.length));
                                }

                                data.playerList = list;
                            }
                        } else {
                            data.status = (new TranslatableComponent("multiplayer.status.unknown")).withStyle(ChatFormatting.DARK_GRAY);
                        }

                        String s = null;
                        if (serverstatus.getFavicon() != null) {
                            String s1 = serverstatus.getFavicon();
                            if (s1.startsWith("data:image/png;base64,")) {
                                s = s1.substring("data:image/png;base64,".length());
                            } else {
                                LOGGER.error("Invalid server icon (unknown format)");
                            }
                        }

                        if (!Objects.equals(s, data.getIconB64())) {
                            data.setIconB64(s);
                            runnable.run();
                        }

                        this.pingStart = Util.getMillis();
                        connection.send(new ServerboundPingRequestPacket(this.pingStart));
                        this.success = true;
                    }
                }

                public void handlePongResponse(ClientboundPongResponsePacket response) {
                    long start = this.pingStart;
                    long current = Util.getMillis();
                    data.ping = current - start;
                    connection.disconnect(new TranslatableComponent("multiplayer.status.finished"));
                }

                public void onDisconnect(Component message) {
                    if (!this.success) {
                        ServerPinger.this.onPingFailed(message, data);
                    }
                }

                @Override
                public Connection getConnection() {
                    return connection;
                }
            });
            try {
                // Why does this make the server ping response work as you'd expect? it greatly increases
                System.out.print("");
                connection.send(new ClientIntentionPacket(serverAddress.getHost(), serverAddress.getPort(), ConnectionProtocol.STATUS));
                connection.send(new ServerboundStatusRequestPacket());
            } catch (Throwable error) {
                LOGGER.error("Failed to ping server {}", data, error);
            }
        }
    }

    static Component formatPlayerCount(int player, int max) {
        return (new TextComponent(Integer.toString(player))).append((new TextComponent("/")).withStyle(ChatFormatting.DARK_GRAY)).append(Integer.toString(max)).withStyle(ChatFormatting.GRAY);
    }

    void onPingFailed(Component pReason, ServerData pServerData) {
        LOGGER.error("Can't ping {}: {}", pServerData.ip, pReason.getString());
        pServerData.motd = CANT_CONNECT_MESSAGE;
        pServerData.status = TextComponent.EMPTY;
    }
}
