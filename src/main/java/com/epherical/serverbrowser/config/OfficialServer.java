package com.epherical.serverbrowser.config;


import com.epherical.epherolib.libs.org.spongepowered.configurate.ConfigurationNode;
import com.epherical.epherolib.libs.org.spongepowered.configurate.serialize.SerializationException;
import com.epherical.epherolib.libs.org.spongepowered.configurate.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Type;

public class OfficialServer {

    public String name;
    public String ipAddress;

    public OfficialServer(String name, String ipAddress) {
        this.name = name;
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class Serializer implements TypeSerializer<OfficialServer> {
        public static final Serializer INSTANCE = new Serializer();

        public final String TAG_NAME = "name";
        public final String TAG_IP_ADDRESS = "ipAddress";


        @Override
        public OfficialServer deserialize(Type type, ConfigurationNode node) throws SerializationException {
            return new OfficialServer(node.node(TAG_NAME).getString(), node.node(TAG_IP_ADDRESS).getString());
        }

        @Override
        public void serialize(Type type, @Nullable OfficialServer obj, ConfigurationNode node) throws SerializationException {
            if (obj == null) {
                node.raw(null);
                return;
            }

            node.node(TAG_NAME).set(obj.name);
            node.node(TAG_IP_ADDRESS).set(obj.ipAddress);
        }
    }
}
