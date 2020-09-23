package lordpipe.vaultjwt;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

// we keep track of who has been issued a token so that we can update roles
// in the future using `/bulktoken`

public class IssuedTokensList {
    public YamlConfiguration config = new YamlConfiguration();

    File issuedListFile;

    public IssuedTokensList() throws IOException, InvalidConfigurationException {
        issuedListFile = new File("vaultjwt-issued-list.yml");

        if (!issuedListFile.exists()) {
            issuedListFile.createNewFile();
        }

        config.load(issuedListFile);
    }

    public List<String> getPlayers() {
        return config.getStringList("issued");
    }

    public void issueForPlayer(OfflinePlayer player) throws IOException {
        String uuid = player.getUniqueId().toString();

        List<String> alreadyIssued = getPlayers();

        if (!alreadyIssued.contains(uuid)) {
            alreadyIssued.add(uuid);
            config.set("issued", alreadyIssued);

            config.save(issuedListFile);
        }
    }
}
