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
import java.util.Optional;

public class ServerPinger {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Component CANT_CONNECT_MESSAGE = Component.translatable("multiplayer.status.cannot_connect").withStyle((style) -> style.withColor(-65536));

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
            data.motd = Component.translatable("multiplayer.status.pinging");
            data.ping = -1L;
            data.playerList = Collections.emptyList();
            connection.setListener(new ClientStatusPacketListener() {
                private boolean success;
                private boolean receivedPing;
                private long pingStart;

                public void handleStatusResponse(ClientboundStatusResponsePacket responsePacket) {
                    if (this.receivedPing) {
                        connection.disconnect(Component.translatable("multiplayer.status.unrequested"));
                    } else {
                        this.receivedPing = true;
                        ServerStatus serverStatus = responsePacket.status();
                        data.motd = serverStatus.description();
                        serverStatus.version().ifPresentOrElse((version) -> {
                            data.version = Component.literal(version.name());
                            data.protocol = version.protocol();
                        }, () -> {
                            data.version = Component.translatable("multiplayer.status.old");
                            data.protocol = 0;
                        });
                        serverStatus.players().ifPresentOrElse((players) -> {
                            data.status = ServerPinger.formatPlayerCount(players.online(), players.max());
                            data.players = players;
                            if (!players.sample().isEmpty()) {
                                List<Component> messages = new ArrayList<>(players.sample().size());

                                for (GameProfile profile : players.sample()) {
                                    messages.add(Component.literal(profile.getName()));
                                }

                                if (players.sample().size() < players.online()) {
                                    messages.add(Component.translatable("multiplayer.status.and_more", players.online() - players.sample().size()));
                                }

                                data.playerList = messages;
                            } else {
                                data.playerList = List.of();
                            }

                        }, () -> data.status = Component.translatable("multiplayer.status.unknown").withStyle(ChatFormatting.DARK_GRAY));
                        serverStatus.favicon().ifPresent(($$2) -> {
                            if (!Arrays.equals($$2.iconBytes(), data.getIconBytes())) {
                                data.setIconBytes($$2.iconBytes());
                                runnable.run();
                            }

                        });
                        this.pingStart = Util.getMillis();
                        connection.send(new ServerboundPingRequestPacket(this.pingStart));
                        this.success = true;
                    }
                }

                public void handlePongResponse(ClientboundPongResponsePacket response) {
                    long start = this.pingStart;
                    long current = Util.getMillis();
                    data.ping = current - start;
                    connection.disconnect(Component.translatable("multiplayer.status.finished"));
                }

                public void onDisconnect(Component message) {
                    if (!this.success) {
                        ServerPinger.this.onPingFailed(message, data);
                    }
                }

                public boolean isAcceptingMessages() {
                    return connection.isConnected();
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

    static Component formatPlayerCount(int $$0, int $$1) {
        return Component.literal(Integer.toString($$0)).append(Component.literal("/").withStyle(ChatFormatting.DARK_GRAY)).append(Integer.toString($$1)).withStyle(ChatFormatting.GRAY);
    }

    public void onPingFailed(Component message, ServerData server) {
        LOGGER.error("Can't ping {}: {}", server.ip, message.getString());
        server.motd = CANT_CONNECT_MESSAGE;
        server.status = CommonComponents.EMPTY;
    }
}
