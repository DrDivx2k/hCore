package com.hakan.core.scoreboard;

import com.hakan.core.HCore;
import com.hakan.core.utils.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * HScoreboard class for creating a
 * scoreboard object for player
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class HScoreboard {

    private final UUID uid;
    private final Scoreboard scoreboard;
    private final Objective objective;

    private String title = "";
    private int updateInterval = 0;

    /**
     * Creates new Instance of this class.
     *
     * @param uid UID of player
     */
    HScoreboard(@Nonnull UUID uid) {
        this.uid = Validate.notNull(uid, "uuid cannot be null");
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = this.scoreboard.registerNewObjective("board", "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(this.title);
    }

    /**
     * Checks if scoreboard still exist for player
     *
     * @return if scoreboard still exist for player, return true
     */
    public boolean isExist() {
        return HScoreboardHandler.findByUID(this.uid).isPresent();
    }

    /**
     * Gets UUID of player.
     *
     * @return UUID of player.
     */
    @Nonnull
    public UUID getUID() {
        return this.uid;
    }

    /**
     * Gets player.
     *
     * @return Player.
     */
    @Nonnull
    public Optional<Player> getPlayerSafe() {
        Player player = Bukkit.getPlayer(this.uid);
        return Optional.ofNullable(player);
    }

    /**
     * Gets player.
     *
     * @return Player.
     */
    @Nonnull
    public Player getPlayer() {
        return this.getPlayerSafe().orElseThrow(() -> new NullPointerException("there is no player with this uid(" + this.uid + ")"));
    }

    /**
     * Gets title of scoreboard.
     *
     * @return Title of scoreboard.
     */
    @Nonnull
    public String getTitle() {
        return this.title;
    }

    /**
     * Sets title of scoreboard.
     *
     * @param title Title.
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard setTitle(@Nonnull String title) {
        this.title = Validate.notNull(title, "title cannot be null");
        this.objective.setDisplayName(title);
        return this;
    }

    /**
     * Gets update interval of scoreboard.
     *
     * @return Update interval of scoreboard.
     */
    public int getUpdateInterval() {
        return this.updateInterval;
    }

    /**
     * Sets update interval of scoreboard.
     *
     * @param updateInterval Update interval.
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
        return this;
    }

    /**
     * Gets text of line.
     *
     * @param line Line.
     * @return Text of line.
     */
    @Nonnull
    public String getLine(int line) {
        return this.getTeam(line).getPrefix();
    }

    /**
     * Sets line of scoreboard to text.
     *
     * @param line Line number.
     * @param text Text.
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard setLine(int line, @Nonnull String text) {
        Validate.notNull(text, "text cannot be null");
        this.getTeam(line).setPrefix(text);
        return this;
    }

    /**
     * Sets lines of scoreboard to lines.
     *
     * @param lines List of lines.
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard setLines(@Nonnull List<String> lines) {
        Validate.notNull(lines, "lines cannot be null");
        for (int i = 1; i <= 16; i++)
            if (lines.size() >= i) this.setLine(i, lines.get(i - 1));
            else this.removeLine(i);
        return this;
    }

    /**
     * Sets lines of scoreboard to lines.
     *
     * @param lines List of lines.
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard setLines(@Nonnull String... lines) {
        return this.setLines(Arrays.asList(lines));
    }

    /**
     * Removes line from scoreboard.
     *
     * @param line Line number.
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard removeLine(int line) {
        Team currentTeam = this.scoreboard.getTeam("line_" + line);
        if (currentTeam == null) {
            return this;
        }

        this.scoreboard.resetScores(currentTeam.getEntries().iterator().next());
        currentTeam.unregister();
        return this;
    }

    /**
     * When the time is up, scoreboard will
     * remove automatically.
     *
     * @param time     Time.
     * @param timeUnit Time unit (TimeUnit.SECONDS, TimeUnit.HOURS, etc.)
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard expire(int time, @Nonnull TimeUnit timeUnit) {
        Validate.notNull(timeUnit, "time unit cannot be null");
        HCore.syncScheduler().after(time, timeUnit)
                .run(this::delete);
        return this;
    }

    /**
     * Shows the scoreboard to player.
     */
    @Nonnull
    public HScoreboard show() {
        this.getPlayerSafe().ifPresent(player -> {
            if (player.getScoreboard().equals(this.scoreboard))
                return;

            player.setScoreboard(this.scoreboard);
        });
        return this;
    }

    /**
     * Once every updateInterval ticks,
     * it will trigger.
     *
     * @param consumer Callback.
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard update(@Nonnull Consumer<HScoreboard> consumer) {
        Validate.notNull(consumer, "consumer cannot be null");

        if (this.updateInterval > 0 && this.isExist()) {
            consumer.accept(this);
            HCore.syncScheduler().after(this.updateInterval)
                    .run(() -> this.update(consumer));
        }
        return this;
    }

    /**
     * Deletes scoreboard.
     *
     * @return Instance of this class.
     */
    @Nonnull
    public HScoreboard delete() {
        this.getPlayerSafe().ifPresent(player -> {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            HScoreboardHandler.getContent().remove(this.uid);
        });
        return this;
    }


    /**
     * Gets or creates team object.
     *
     * @param line Line.
     * @return Team.
     */
    @Nonnull
    private Team getTeam(int line) {
        Team currentTeam = this.scoreboard.getTeam("line_" + line);
        if (currentTeam != null) {
            return currentTeam;
        }

        Team newTeam = this.scoreboard.registerNewTeam("line_" + line);
        newTeam.setAllowFriendlyFire(true);
        newTeam.setCanSeeFriendlyInvisibles(false);
        if (newTeam.getEntries().size() > 0) {
            newTeam.removeEntry(newTeam.getEntries().iterator().next());
        }

        String teamEntry = line >= 10 ? "§" + new String[]{"a", "b", "c", "d", "e", "f"}[line - 10] : "§" + line;
        newTeam.addEntry(teamEntry);

        this.objective.getScore(teamEntry).setScore(16 - line);
        return newTeam;
    }
}