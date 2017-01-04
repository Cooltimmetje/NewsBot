package me.Cooltimmetje.NewsBot.Utilities;

import me.Cooltimmetje.NewsBot.Database.MySqlManager;
import me.Cooltimmetje.NewsBot.Main;
import me.Cooltimmetje.NewsBot.Objects.Faction;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RoleBuilder;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.HashMap;

/**
 * Manages factions.
 *
 * @author Tim (Cooltimmetje)
 * @version v0.1-ALPHA-DEV
 * @since v0.1-ALPHA-DEV
 */
public class FactionManager {

    private static HashMap<Integer,Faction> factions = new HashMap<>();

    /**
     * This creates a new faction and saves it into the database. Also makes the channels and roles and sets the permissions properly.
     *
     * @param name The name of the Faction.
     */
    public static void createNewFaction(String name, IChannel channelSent) throws RateLimitException, DiscordException, MissingPermissionsException {
        IGuild guild = Main.getInstance().getNewsBot().getGuildByID(Constants.SERVER_ID);
        IChannel channel = guild.createChannel(name.replace("'", "").replace(" ", "-"));
        RoleBuilder rb = new RoleBuilder(guild).withName(name).withPermissions(EnumSet.of(Permissions.VOICE_CONNECT));
        IRole role = rb.build();

        channel.overrideRolePermissions(role, EnumSet.of(Permissions.READ_MESSAGES), null);
        channel.overrideRolePermissions(guild.getEveryoneRole(), null, EnumSet.of(Permissions.READ_MESSAGES));
        channel.overrideRolePermissions(guild.getRolesByName("Moderator").get(0), EnumSet.of(Permissions.READ_MESSAGES), null);

        Faction faction = new Faction(name, channel.getID(), role.getID(), "1");

        MySqlManager.addFaction(faction);
        int id = MySqlManager.fetchFactionId(channel.getID());
        faction.setId(id);

        factions.put(id, faction);
        IMessage message = MessagesUtils.sendPlain(MessageFormat.format("**{0}** | ID: **{1}**", name, id), channel.getGuild().getChannelByID("266287084719964161"));
        faction.setMessageId(message.getID());
        faction.save();

        Logger.info(MessageFormat.format("[Factions][Created] New faction has been created: {0} (ID: {1}) - Channel ID: {2} | Role ID: {3} | Message ID: {4}", name, id, channel.getID(), role.getID(), message.getID()));
        MessagesUtils.sendSuccess("Faction **" + name + "** has been added! :tada:" , channelSent);
    }

    /**
     * Load a faction with the given data.
     *
     * @param id ID of the faction.
     * @param name The name of the faction.
     * @param channelId The channel ID of the factions channel.
     * @param roleId The role ID of the factions role.
     * @param messageId The message ID of the factions message.
     */
    public static void loadFaction(int id, String name, String channelId, String roleId, String messageId){
        Faction faction = new Faction(id, name, channelId, roleId, messageId);
        factions.put(id, faction);

        Logger.info(MessageFormat.format("[Factions][Loaded] Faction has been loaded: {0} (ID: {1}) - Channel ID: {2} | Role ID: {3} | Message ID: {4}", name, id, channelId, roleId, messageId));
    }

    /**
     * Gets the faction by ID.
     *
     * @param id The ID of the faction that we want.
     * @return The faction instance. - Can return null! Handle properly!
     */
    public static Faction getFaction(int id){
        if(factions.containsKey(id)) {
            return factions.get(id);
        } else {
            return null;
        }
    }
}
