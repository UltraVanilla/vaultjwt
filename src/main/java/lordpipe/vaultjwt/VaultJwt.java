package lordpipe.vaultjwt;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import lordpipe.vaultjwt.commands.TokenCommand;
import lordpipe.vaultjwt.commands.BulkTokenCommand;
import net.milkbowl.vault.permission.Permission;

public class VaultJwt extends JavaPlugin {
    public Permission permissions;
    public ECPrivateKey privKey;

    public IssuedTokensList issuedList;
    public FileConfiguration config = getConfig();

    public TokenGenerator tokenGenerator = new TokenGenerator(this);

    public static VaultJwt instance;

    @Override
    public void onEnable() {
        getLogger().info("VaultJwt loaded");

        instance = this;

        this.saveDefaultConfig();

        try {
            issuedList = new IssuedTokensList();
        } catch (IOException | InvalidConfigurationException err) {
            err.printStackTrace();
            crash("Could not read issued tokens list for VaultJwt");
            return;
        }

        if (!readKey()) return;

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            crash("Needs vault permission provider");
            return;
        }
        permissions = rsp.getProvider();

        this.getCommand("token").setExecutor(new TokenCommand(this));
        this.getCommand("bulktoken").setExecutor(new BulkTokenCommand(this));
    }
    @Override
    public void onDisable() {
        getLogger().info("VaultJwt unloaded");
    }

    public void crash(String err) {
        getLogger().warning(err);
        Bukkit.getPluginManager().disablePlugin(this);
    }

    private boolean readKey() {
        StringReader reader = new StringReader(config.getString("privKey"));
        PEMParser parser = new PEMParser(reader);

        PemObject pem;

        try {
            pem = parser.readPemObject();
        } catch (IOException err) {
            err.printStackTrace();
            crash("Could not parse PEM data for VaultJwt");
            return false;
        }

        if (pem == null) {
            crash("Could not parse PEM data for VaultJwt");
            return false;
        }

        byte[] contents = pem.getContent();
        reader.close();

        // extract private key
        try {
            KeyFactory kf = KeyFactory.getInstance("EC");
            EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(contents);
            privKey = (ECPrivateKey) kf.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException err) {
            // unreachable... unreachable? we'll find out soon!
            return false;
        } catch (InvalidKeySpecException err) {
            crash("Could not parse PEM data for VaultJwt - Invalid key spec");
            return false;
        }

        // why does this dumb API give so many silent `null`s
        if (privKey == null) {
            crash("Could not parse PEM data for VaultJwt - generatePrivate returned null");
            return false;
        }

        getLogger().info("Successfully loaded private key for signing VaultJwt tokens");

        return true;
    }
}
