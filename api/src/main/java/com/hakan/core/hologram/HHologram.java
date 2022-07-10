package com.hakan.core.hologram;

import com.hakan.core.HCore;
import com.hakan.core.hologram.line.HHologramLine;
import com.hakan.core.renderer.HRenderer;
import com.hakan.core.utils.Validate;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Hologram class to create and
 * manage the created hologram.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class HHologram {

    private final String id;
    private final List<HHologramLine> lines;
    private double lineDistance = 0.25;
    private HRenderer renderer;

    /**
     * Creates new instance of this class.
     *
     * @param id         Hologram id that you want.
     * @param location   Hologram location.
     * @param playerList List of player who can see hologram.
     */
    HHologram(@Nonnull String id, @Nonnull Location location, @Nonnull Set<UUID> playerList) {
        Validate.notNull(id, "id cannot be null!");
        Validate.notNull(location, "location cannot be null!");
        Validate.notNull(playerList, "player list cannot be null!");

        this.id = id;
        this.lines = new LinkedList<>();
        this.renderer = new HRenderer(location, 30, playerList,
                players -> this.lines.forEach(line -> line.show(players)),
                players -> this.lines.forEach(line -> line.hide(players)),
                renderer -> this.lines.forEach(line -> line.hide(renderer.getShownViewersAsPlayer()))
        ).showEveryone(false);
    }

    /**
     * Creates new instance of this class.
     *
     * @param id       Hologram id that you want.
     * @param location Hologram location.
     */
    HHologram(@Nonnull String id, @Nonnull Location location) {
        Validate.notNull(id);
        Validate.notNull(location);

        this.id = id;
        this.lines = new LinkedList<>();
        this.renderer = new HRenderer(location, 30,
                players -> this.lines.forEach(line -> line.show(players)),
                players -> this.lines.forEach(line -> line.hide(players)),
                renderer -> this.lines.forEach(line -> line.hide(renderer.getShownViewersAsPlayer()))
        ).showEveryone(true);
    }

    /**
     * Is hologram deleted?
     *
     * @return If hologram was deleted, returns true.
     */
    public boolean isExist() {
        return HHologramHandler.getContent().containsKey(this.id);
    }

    /**
     * Gets id of hologram.
     *
     * @return ID of hologram.
     */
    @Nonnull
    public String getId() {
        return this.id;
    }

    /**
     * Gets location of hologram.
     *
     * @return Location of hologram.
     */
    @Nonnull
    public Location getLocation() {
        return this.renderer.getLocation();
    }

    /**
     * Gets distance between two lines of hologram.
     *
     * @return Distance between two lines of hologram.
     */
    public double getLineDistance() {
        return this.lineDistance;
    }

    /**
     * Sets distance between two lines.
     *
     * @param hologramDistance Distance between two lines.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram setLineDistance(double hologramDistance) {
        this.lineDistance = hologramDistance;
        this.setLocation(this.getLocation());
        return this;
    }

    /**
     * Gets renderer.
     *
     * @return renderer.
     */
    @Nonnull
    public HRenderer getRenderer() {
        return this.renderer;
    }

    /**
     * Sets renderer of hologram.
     *
     * @param renderer Renderer.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram setRenderer(@Nonnull HRenderer renderer) {
        this.renderer = Validate.notNull(renderer, "renderer cannot be null!");
        return this;
    }

    /**
     * If everyone can see hologram,
     * returns true.
     *
     * @return If everyone can see hologram, returns true.
     */
    public boolean canEveryoneSee() {
        return this.renderer.canSeeEveryone();
    }

    /**
     * If set this as true, everyone
     * can see this hologram.
     *
     * @param mode Mode status.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram showEveryone(boolean mode) {
        this.renderer.showEveryone(mode);
        return this;
    }

    /**
     * Gets hologram lines.
     *
     * @return Hologram lines.
     */
    @Nonnull
    public List<HHologramLine> getLines() {
        return new LinkedList<>(this.lines);
    }

    /**
     * Gets hologram line from index.
     *
     * @param index Index.
     * @return HHologramLine class.
     */
    @Nonnull
    public HHologramLine getLine(int index) {
        if (index > this.lines.size() || index < 0)
            throw new IndexOutOfBoundsException("index cannot bigger line size or smaller than 0 :" + index);
        return this.lines.get(index);
    }

    /**
     * NPC expires after a certain time.
     *
     * @param expire   Amount.
     * @param timeUnit Time Unit.
     * @return HNPC for chain.
     */
    @Nonnull
    public HHologram expire(int expire, @Nonnull TimeUnit timeUnit) {
        Validate.notNull(timeUnit, "time unit cannot be null!");
        HCore.syncScheduler().after(expire, timeUnit).run(this::delete);
        return this;
    }

    /**
     * NPC expires after a certain time.
     *
     * @param duration Duration.
     * @return HNPC for chain.
     */
    @Nonnull
    public HHologram expire(@Nonnull Duration duration) {
        Validate.notNull(duration, "duration cannot be null!");
        HCore.syncScheduler().after(duration).run(this::delete);
        return this;
    }

    /**
     * If you want to remove the hologram
     * when the time is up. You can use this.
     *
     * @param ticks Time. (as tick)
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram expire(long ticks) {
        HCore.syncScheduler().after(ticks)
                .run(this::delete);
        return this;
    }

    /**
     * Adds player to hologram to show.
     *
     * @param players Player list.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addViewer(@Nonnull Collection<Player> players) {
        Validate.notNull(players, "players cannot be null");
        players.forEach(player -> this.renderer.addViewer(player.getUniqueId()));
        return this;
    }

    /**
     * Adds player to hologram to show.
     *
     * @param players Player list.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addViewer(@Nonnull Player... players) {
        Validate.notNull(players, "players cannot be null");
        Arrays.asList(players).forEach(this.renderer::addViewer);
        return this;
    }

    /**
     * Adds player to hologram to show.
     *
     * @param uids UID list.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addViewerByUID(@Nonnull Collection<UUID> uids) {
        Validate.notNull(uids, "uuids cannot be null");
        uids.forEach(this.renderer::addViewer);
        return this;
    }

    /**
     * Adds player to hologram to show.
     *
     * @param uids UID list.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addViewerByUID(@Nonnull UUID... uids) {
        Validate.notNull(uids, "uuids cannot be null");
        Arrays.asList(uids).forEach(this.renderer::addViewer);
        return this;
    }

    /**
     * Removes player from hologram to hide.
     *
     * @param players Player list.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram removeViewer(@Nonnull Collection<Player> players) {
        Validate.notNull(players, "players cannot be null");
        players.forEach(player -> this.renderer.removeViewer(player.getUniqueId()));
        return this;
    }

    /**
     * Removes player from hologram to hide.
     *
     * @param players Player list.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram removeViewer(@Nonnull Player... players) {
        Validate.notNull(players, "players cannot be null");
        Arrays.asList(players).forEach(this.renderer::removeViewer);
        return this;
    }

    /**
     * Removes player from hologram to hide.
     *
     * @param uids UID list.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram removeViewerByUID(@Nonnull Collection<UUID> uids) {
        Validate.notNull(uids, "players cannot be null");
        uids.forEach(this.renderer::removeViewer);
        return this;
    }

    /**
     * Removes player from hologram to hide.
     *
     * @param uids UID list.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram removeViewerByUID(@Nonnull UUID... uids) {
        Validate.notNull(uids, "players cannot be null");
        Arrays.asList(uids).forEach(this.renderer::removeViewer);
        return this;
    }

    /**
     * Sets location of hologram.
     *
     * @param location Location to teleport.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram setLocation(@Nonnull Location location) {
        Validate.notNull(location, "location cannot be null");
        Location loc = location.clone().add(0, (this.lineDistance / 2) * (this.lines.size() + 2), 0);
        this.renderer.setLocation(location);
        this.lines.forEach(hHologramLine -> hHologramLine.setLocation(loc.subtract(0, this.lineDistance, 0)));
        return this;
    }

    /**
     * Adds new lines to hologram.
     *
     * @param hHologramLine HHologramLine classes.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addLines(@Nonnull HHologramLine... hHologramLine) {
        Validate.notNull(hHologramLine, "hHologramLine cannot be null");
        Arrays.asList(hHologramLine).forEach(this::addLine);
        return this;
    }

    /**
     * Adds new lines to hologram.
     *
     * @param texts Text.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addLines(@Nonnull Collection<String> texts) {
        Validate.notNull(texts, "texts cannot be null");
        texts.forEach(this::addLine);
        return this;
    }

    /**
     * Adds new lines to hologram.
     *
     * @param texts text.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addLines(@Nonnull String... texts) {
        Validate.notNull(texts, "texts cannot be null");
        Arrays.asList(texts).forEach(this::addLine);
        return this;
    }

    /**
     * Adds new line to hologram.
     *
     * @param hHologramLine HHologramLine class.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addLine(@Nonnull HHologramLine hHologramLine) {
        Validate.notNull(hHologramLine, "hHologramLine cannot be null");

        this.lines.add(hHologramLine);
        this.setLocation(this.getLocation());
        hHologramLine.show(this.renderer.getShownViewersAsPlayer());

        return this;
    }

    /**
     * Adds new text line to hologram.
     *
     * @param text Text.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram addLine(@Nonnull String text) {
        return this.addLine(new HHologramLine(this, text));
    }

    /**
     * Removes line from hologram.
     *
     * @param indexes Indexes.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram removeLines(@Nonnull Collection<Integer> indexes) {
        Validate.notNull(indexes, "indexes cannot be null");
        indexes.forEach(this::removeLine);
        return this;
    }

    /**
     * Removes line from hologram.
     *
     * @param indexes Indexes.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram removeLines(int... indexes) {
        for (int index : indexes)
            this.removeLine(index);
        return this;
    }

    /**
     * Removes line from hologram.
     *
     * @param index Index.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram removeLine(int index) {
        HHologramLine hHologramLine = this.lines.remove(index);
        hHologramLine.hide(this.renderer.getShownViewersAsPlayer());
        this.setLocation(this.getLocation());
        return this;
    }

    /**
     * Clear lines from hologram.
     *
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram clearLines() {
        for (int i = this.lines.size() - 1; i >= 0; i--)
            this.removeLine(i);
        return this;
    }

    /**
     * Replaces line at row with text.
     *
     * @param row  Index of line.
     * @param text Text.
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram setLine(int row, @Nonnull String text) {
        Validate.notNull(text, "text cannot be null");
        this.getLine(row).setText(text);
        return this;
    }

    /**
     * Deletes hologram.
     *
     * @return Instance of this class.
     */
    @Nonnull
    public HHologram delete() {
        if (this.isExist()) {
            this.renderer.delete();
            this.lines.clear();
            HHologramHandler.getContent().remove(this.id);
        }
        return this;
    }
}