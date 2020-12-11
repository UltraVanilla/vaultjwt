package lordpipe.vaultjwt.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import lordpipe.vaultjwt.VaultJwt;

public class BulkTokenCommand implements CommandExecutor {
    private VaultJwt plugin;

    public BulkTokenCommand(VaultJwt instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        HashSet<String> players = new HashSet<String>(plugin.issuedList.getPlayers());

        try {
            // add all currently online players
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                players.add(player.getUniqueId().toString());
                plugin.issuedList.issueForPlayer(player);
            }
        } catch (IOException err) {
            err.printStackTrace();
            sender.sendMessage("Something has gone horribly wrong");
            return false;
        }

        // must do offline player lookups off-thread
        Thread thread = new Thread() {
            public void run() {

                Claims claims = Jwts.claims();

                ArrayList<Object> playerTokenList = new ArrayList();
                for (String uuid : players) {
                    HashMap<String, Object> tokenObj = new HashMap();

                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

                    String[] perms = plugin.permissions.getPlayerGroups(null, player);
                    tokenObj.put("uuid", player.getUniqueId().toString());
                    tokenObj.put("groups", perms);
                    tokenObj.put("disallowAuth", true);
                    playerTokenList.add(tokenObj);
                }

                claims.put("players", playerTokenList);


                sender.sendMessage(plugin.tokenGenerator.genTokenWithClaims(claims, "bulk"));
            }
        };
        thread.start();

        return true;
    }
}
