package com.hakan.core.npc.wrapper;

import com.hakan.core.HCore;
import com.hakan.core.npc.HNPC;
import com.hakan.core.npc.HNPCHandler;
import com.hakan.core.npc.skin.HNPCSkin;
import com.hakan.core.utils.Validate;
import net.minecraft.server.v1_10_R1.EntityArmorStand;
import net.minecraft.server.v1_10_R1.EntityPlayer;
import net.minecraft.server.v1_10_R1.EnumItemSlot;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntity;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_10_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_10_R1.PacketPlayOutMount;
import net.minecraft.server.v1_10_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_10_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_10_R1.PacketPlayOutSpawnEntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * {@inheritDoc}
 */
public final class HNPC_v1_10_R1 extends HNPC {

    private final HNPCUtils_v1_10_R1 utils;
    private EntityPlayer npc;
    private EntityArmorStand armorStand;

    /**
     * {@inheritDoc}
     */
    public HNPC_v1_10_R1(@Nonnull String id,
                         @Nonnull HNPCSkin skin,
                         @Nonnull Location location,
                         @Nonnull List<String> lines,
                         @Nonnull Set<UUID> viewers,
                         @Nonnull Map<EquipmentType, ItemStack> equipments,
                         boolean showEveryone) {
        super(id, location, lines, viewers, equipments, showEveryone);
        super.showEveryone(showEveryone);

        this.utils = new HNPCUtils_v1_10_R1();

        this.npc = this.utils.createNPC(skin, location);
        this.armorStand = this.utils.createNameHider(location);
        this.npc.passengers.clear();
        this.npc.passengers.add(this.armorStand);

        HCore.syncScheduler().after(20)
                .run(() -> this.hide(super.renderer.getShownViewersAsPlayer()));
        HCore.syncScheduler().after(25)
                .run(() -> this.show(super.renderer.getShownViewersAsPlayer()));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public HNPC walk(@Nonnull Location to, double speed) {
        Validate.notNull(to, "to location cannot be null!");

        if (this.walking)
            throw new IllegalStateException("NPC is already walking!");

        super.walking = true;
        this.utils.walk(this, to, speed, () -> super.walking = false);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public HNPC setLocation(@Nonnull Location location) {
        Validate.notNull(location, "location cannot be null!");

        this.npc.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        super.hologram.setLocation(location.clone().add(0, this.npc.getHeadHeight() + (this.hologram.getLines().size() * 0.125), 0));
        super.renderer.setLocation(location);

        double imp = 256f / 360f;
        float yaw = Math.round(location.getYaw() % 360f * imp);
        float pitch = Math.round(location.getPitch() % 360f * imp);
        HCore.sendPacket(super.renderer.getShownViewersAsPlayer(),
                new PacketPlayOutEntityHeadRotation(this.npc, (byte) (location.getYaw() * imp)),
                new PacketPlayOutEntity.PacketPlayOutEntityLook(this.npc.getId(), (byte) yaw, (byte) pitch, false),
                new PacketPlayOutEntityTeleport(this.npc)
        );

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEntityID() {
        return this.npc.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public HNPC setSkin(@Nonnull HNPCSkin skin) {
        Validate.notNull(skin, "skin cannot be null!");

        List<Player> players = super.renderer.getShownViewersAsPlayer();

        this.hide(players);
        this.npc = this.utils.createNPC(skin, super.getLocation());
        this.armorStand = this.utils.createNameHider(super.getLocation());
        this.npc.passengers.clear();
        this.npc.passengers.add(this.armorStand);
        HCore.syncScheduler().after(20).run(() -> this.show(players));

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public HNPC setEquipment(@Nonnull EquipmentType equipment, @Nonnull ItemStack itemStack) {
        Validate.notNull(equipment, "equipment type cannot be null!");
        Validate.notNull(itemStack, "itemStack type cannot be null!");

        super.equipments.put(equipment, itemStack);

        HCore.sendPacket(super.renderer.getShownViewersAsPlayer(),
                new PacketPlayOutEntityEquipment(this.npc.getId(), EnumItemSlot.valueOf(equipment.name()), CraftItemStack.asNMSCopy(itemStack)));

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public HNPC show(@Nonnull List<Player> players) {
        HCore.sendPacket(Validate.notNull(players, "players cannot be null!"),
                new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, this.npc),
                new PacketPlayOutNamedEntitySpawn(this.npc),
                new PacketPlayOutEntityMetadata(this.npc.getId(), this.utils.createDataWatcher(this.npc), true),

                new PacketPlayOutSpawnEntityLiving(this.armorStand),
                new PacketPlayOutEntityMetadata(this.armorStand.getId(), this.armorStand.getDataWatcher(), true),
                new PacketPlayOutEntityTeleport(this.armorStand),
                new PacketPlayOutMount(this.npc)
        );

        if (super.equipments.size() > 0) {
            super.equipments.forEach((key, value) -> HCore.sendPacket(players,
                    new PacketPlayOutEntityEquipment(this.npc.getId(), EnumItemSlot.valueOf(key.name()), CraftItemStack.asNMSCopy(value))));
        }

        HCore.asyncScheduler().after(5)
                .run(() -> HCore.sendPacket(players, new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, this.npc)));
        return this.setLocation(super.getLocation());
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public HNPC hide(@Nonnull List<Player> players) {
        Validate.notNull(players, "players cannot be null!");

        HCore.sendPacket(players,
                new PacketPlayOutEntityDestroy(this.npc.getId(), this.armorStand.getId()));

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public HNPC delete() {
        HNPCHandler.getContent().remove(super.id);

        super.action.onDelete();
        super.hologram.delete();
        super.renderer.delete();
        super.dead = true;
        super.walking = false;
        return this.hide(super.renderer.getShownViewersAsPlayer());
    }
}