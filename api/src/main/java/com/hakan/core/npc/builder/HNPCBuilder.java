package com.hakan.core.npc.builder;

import com.hakan.core.npc.HNPC;
import com.hakan.core.npc.HNPCHandler;
import com.hakan.core.npc.skin.HNPCSkin;
import com.hakan.core.utils.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * HNPCBuilder class to build
 * NPCs' easily.
 */
public final class HNPCBuilder {

    private final String id;
    private Boolean show;
    private HNPCSkin skin;
    private Location location;
    private Set<UUID> viewers;
    private List<String> lines;
    private Map<HNPC.EquipmentType, ItemStack> equipments;

    private long clickDelay;
    private Consumer<HNPC> spawnConsumer;
    private Consumer<HNPC> deleteConsumer;
    private BiConsumer<Player, HNPC.Action> clickConsumer;


    /**
     * Constructor to create builder.
     *
     * @param id NPC id.
     */
    public HNPCBuilder(@Nonnull String id) {
        this.id = Validate.notNull(id, "id cannot be null!");
        this.skin = HNPCSkin.EMPTY;
        this.lines = new ArrayList<>();
        this.viewers = new HashSet<>();
        this.equipments = new HashMap<>();
        this.location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
    }

    /**
     * Can everyone see this npc or not.
     *
     * @param show Show or not.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder showEveryone(boolean show) {
        this.show = show;
        return this;
    }

    /**
     * Set viewers of npc.
     *
     * @param viewers Viewers.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder viewers(@Nonnull UUID... viewers) {
        Validate.notNull(viewers, "viewers cannot be null!");
        this.viewers = new HashSet<>(Arrays.asList(viewers));
        return this;
    }

    /**
     * Set viewers of npc.
     *
     * @param viewers Viewers.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder viewers(@Nonnull Set<UUID> viewers) {
        Validate.notNull(viewers, "viewers cannot be null!");
        this.viewers = viewers;
        return this;
    }

    /**
     * Adds viewers to npc.
     *
     * @param viewers Viewers.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder addViewers(@Nonnull UUID... viewers) {
        Validate.notNull(viewers, "viewers cannot be null!");
        this.viewers.addAll(Arrays.asList(viewers));
        return this;
    }

    /**
     * Adds viewers to npc hologram.
     *
     * @param viewers Viewers.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder addViewers(@Nonnull Set<UUID> viewers) {
        Validate.notNull(viewers, "viewers cannot be null!");
        this.viewers.addAll(viewers);
        return this;
    }

    /**
     * Sets lines of npc hologram.
     *
     * @param lines Lines.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder lines(@Nonnull List<String> lines) {
        Validate.notNull(lines, "lines cannot be null!");
        this.lines = lines;
        return this;
    }

    /**
     * Sets lines of npc hologram.
     *
     * @param lines Lines.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder lines(@Nonnull String... lines) {
        Validate.notNull(lines, "lines cannot be null!");
        this.lines = Arrays.asList(lines);
        return this;
    }

    /**
     * Appends lines to npc hologram.
     *
     * @param lines Lines.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder appendLines(@Nonnull List<String> lines) {
        Validate.notNull(lines, "lines cannot be null!");
        this.lines.addAll(lines);
        return this;
    }

    /**
     * Appends lines to npc hologram.
     *
     * @param lines Lines.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder appendLines(@Nonnull String... lines) {
        Validate.notNull(lines, "lines cannot be null!");
        this.lines.addAll(Arrays.asList(lines));
        return this;
    }

    /**
     * Sets location of npc.
     *
     * @param location Location.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder location(@Nonnull Location location) {
        Validate.notNull(location, "location cannot be null!");
        this.location = location;
        return this;
    }

    /**
     * Sets click action of npc.
     *
     * @param action action on click.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder whenClicked(@Nonnull BiConsumer<Player, HNPC.Action> action) {
        return this.whenClicked(action, 20);
    }

    /**
     * Sets click action of npc.
     *
     * @param action action on click.
     * @param delay  delay between clicks.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder whenClicked(@Nonnull BiConsumer<Player, HNPC.Action> action, long delay) {
        this.clickConsumer = Validate.notNull(action, "action cannot be null!");
        this.clickDelay = Math.max(0, delay);
        return this;
    }

    /**
     * Sets click action of npc.
     *
     * @param action action on spawn.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder whenSpawned(@Nonnull Consumer<HNPC> action) {
        this.spawnConsumer = Validate.notNull(action, "action cannot be null!");
        return this;
    }

    /**
     * Sets click action of npc.
     *
     * @param action action on delete.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder whenDeleted(@Nonnull Consumer<HNPC> action) {
        this.deleteConsumer = Validate.notNull(action, "action cannot be null!");
        return this;
    }

    /**
     * Sets skin of npc.
     *
     * @param skin Skin.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder skin(@Nonnull HNPCSkin skin) {
        this.skin = Validate.notNull(skin, "skin cannot be null!");
        return this;
    }

    /**
     * Sets skin of npc.
     *
     * @param skin Skin.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder skin(@Nonnull String skin) {
        Validate.notNull(skin, "skin cannot be null!");
        return this.skin(HNPCSkin.from(skin));
    }

    /**
     * Sets equipment of npc.
     *
     * @param type Equipment type.
     * @param item Item.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder equipment(@Nonnull HNPC.EquipmentType type, @Nonnull ItemStack item) {
        Validate.notNull(type, "type cannot be null!");
        Validate.notNull(item, "item cannot be null!");
        this.equipments.put(type, item);
        return this;
    }

    /**
     * Sets equipments of npc.
     *
     * @param equipments Equipments.
     * @return HNPCBuilder instance.
     */
    @Nonnull
    public HNPCBuilder equipments(@Nonnull Map<HNPC.EquipmentType, ItemStack> equipments) {
        Validate.notNull(equipments, "equipments cannot be null!");
        this.equipments = equipments;
        return this;
    }

    /**
     * Builds npc.
     *
     * @return HNPC instance.
     */
    @Nonnull
    public HNPC build() {
        if (this.show == null)
            this.show = (this.viewers.size() > 0);

        HNPC npc = new HNPC(this.id, this.location, this.lines, this.viewers, this.equipments, this.show);
        npc.setSkin(this.skin);

        if (this.clickConsumer != null)
            npc.whenClicked(this.clickConsumer);
        if (this.spawnConsumer != null)
            npc.whenSpawned(this.spawnConsumer);
        if (this.deleteConsumer != null)
            npc.whenDeleted(this.deleteConsumer);
        npc.getAction().setClickDelay(this.clickDelay);

        HNPCHandler.getContent().put(this.id, npc);
        return npc;
    }
}