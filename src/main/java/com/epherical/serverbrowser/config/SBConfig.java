package com.epherical.serverbrowser.config;

import com.epherical.epherolib.config.CommonConfig;
import com.epherical.epherolib.libs.org.spongepowered.configurate.CommentedConfigurationNode;
import com.epherical.epherolib.libs.org.spongepowered.configurate.ConfigurateException;
import com.epherical.epherolib.libs.org.spongepowered.configurate.loader.AbstractConfigurationLoader;
import com.epherical.epherolib.libs.org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class SBConfig extends CommonConfig {

    public boolean serverBrowserNotification = false;
    public String bisectPackID = "";
    public String modPackFilter = "";
    public List<OfficialServer> officialServers = new ArrayList<>();
    public List<String> blacklistedServers = new ArrayList<>();


    public SBConfig(AbstractConfigurationLoader.Builder<?, ?> loaderBuilder, String configName) {
        super(loaderBuilder, configName);
       //officialServers.add(new OfficialServer("howdy", "mc.hyupixel.net"));
        //officialServers=[
        //    {
        //        ipAddress="mc.hyupixel.net"
        //        name=howdy
        //    }
        //]
    }

    @Override
    public void parseConfig(CommentedConfigurationNode node) {
        serverBrowserNotification = node.node("serverBrowserNotification").getBoolean(serverBrowserNotification);
        bisectPackID = node.node("bisectPackID").getString(bisectPackID);
        modPackFilter = node.node("modPackFilter").getString(modPackFilter);
        try {
            officialServers = node.node("officialServers").getList(OfficialServer.class, officialServers);
            blacklistedServers = node.node("blacklistedServers").getList(String.class, new ArrayList<>());
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CommentedConfigurationNode generateConfig(CommentedConfigurationNode node) {
        try {
            node.node("serverBrowserNotification").set(serverBrowserNotification).comment("Will determine if the user receives a 'notification' that they can explore the server browser.");
            node.node("bisectPackID").set(bisectPackID).comment("If you want bisect servers to appear in the server listing");
            node.node("modPackFilter").set(modPackFilter).comment("If you have a modpack tag on the minecraft.multiplayerservers.net website you can put the name of the tag in here and it will filter out all other servers");
            node.node("blacklistedServers").setList(String.class, blacklistedServers).comment("If you don't want certain servers to appear on the list (excluding bisect) put the IP of it in here.");
            node.node("officialServers").setList(OfficialServer.class, officialServers).comment("If you have an official server for you pack, you can put it in here and it will appear at the top of the list");
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }

        return node;
    }

    public void saveConfig() {
        try {
            this.loader.save(generateConfig((CommentedConfigurationNode) rootNode));
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    }

}
