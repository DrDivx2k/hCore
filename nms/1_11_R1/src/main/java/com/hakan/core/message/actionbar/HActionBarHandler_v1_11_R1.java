package com.hakan.core.message.actionbar;

import com.hakan.core.HCore;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class HActionBarHandler_v1_11_R1 implements HActionBarHandler {

    @Override
    public void send(@Nonnull Player player, @Nonnull String text) {
        IChatBaseComponent baseComponent = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + text + "\"}");
        HCore.sendPacket(player, new PacketPlayOutChat(baseComponent, (byte) 2));
    }
}
