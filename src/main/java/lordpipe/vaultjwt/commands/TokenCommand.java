package lordpipe.vaultjwt.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import lordpipe.vaultjwt.VaultJwt;

public class TokenCommand implements CommandExecutor {
    private VaultJwt plugin;

    public TokenCommand(VaultJwt instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        OfflinePlayer player;

        if (sender instanceof Player) {
            player = (OfflinePlayer) sender;
        } else if (sender instanceof ConsoleCommandSender && args.length >= 1) {
            // allow the console to generate auth signature for anyone
            player = Bukkit.getOfflinePlayer(args[0]);
        } else {
            sender.sendMessage("Invalid command");
            return false;
        }

        try {
            plugin.issuedList.issueForPlayer(player);
        } catch (IOException err) {
            err.printStackTrace();
            sender.sendMessage("Something has gone horribly wrong");
            return false;
        }

        // must do offline player lookups off-thread
        Thread thread = new Thread() {
            public void run() {
                String[] perms = plugin.permissions.getPlayerGroups(null, player);

                Claims claims = Jwts.claims();

                claims.put("uuid", player.getUniqueId().toString());
                claims.put("groups", perms);

                sender.sendMessage(plugin.tokenGenerator.genTokenWithClaims(claims, "login"));
                sender.sendMessage(plugin.config.getString("message"));
            }
        };
        thread.start();

        return true;
    }
}
