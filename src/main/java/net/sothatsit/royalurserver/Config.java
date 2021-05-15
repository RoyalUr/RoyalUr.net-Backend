package net.sothatsit.royalurserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows use of a config file to edit settings for the RoyalUr server.
 */
public class Config {

    private static final String DEBUG_MODE = "debug-mode";

    private static final String RUN_DISCORD_BOT_KEY = "run-discord-bot";
    private static final String DISCORD_BOT_TOKEN = "discord-bot-token";

    private static final String USE_SSL_KEY = "use-ssl";
    private static final String SSL_CERT_FILE_KEY = "ssl-cert-file";
    private static final String SSL_PRIVATE_KEY_FILE_KEY = "ssl-private-key-file";
    private static final String SSL_PASSWORD_KEY = "ssl-password";

    private static final Logger logger = Logging.getLogger("config");

    private final JSONObject contents;

    public Config() {
        this.contents = new JSONObject();
    }

    public Config(String contents) {
        this.contents = new JSONObject(contents);
    }

    public boolean isDebugMode() {
        return contents.has(DEBUG_MODE) && contents.getBoolean(DEBUG_MODE);
    }

    public boolean runDiscordBot() {
        return contents.has(RUN_DISCORD_BOT_KEY) && contents.getBoolean(RUN_DISCORD_BOT_KEY);
    }

    public String getDiscordToken() {
        return contents.has(DISCORD_BOT_TOKEN) ? contents.getString(DISCORD_BOT_TOKEN) : "";
    }

    public boolean useSSL() {
        return contents.has(USE_SSL_KEY) && contents.getBoolean(USE_SSL_KEY);
    }

    public String getSSLCertFile() {
        return contents.has(SSL_CERT_FILE_KEY) ? contents.getString(SSL_CERT_FILE_KEY) : "";
    }

    public String getSSLPrivateKeyFile() {
        return contents.has(SSL_PRIVATE_KEY_FILE_KEY) ? contents.getString(SSL_PRIVATE_KEY_FILE_KEY) : "";
    }

    public String getSSLPassword() {
        return contents.has(SSL_PASSWORD_KEY) ? contents.getString(SSL_PASSWORD_KEY) : "";
    }

    public JSONObject write() {
        JSONObject output = new JSONObject();
        output.put(RUN_DISCORD_BOT_KEY, runDiscordBot());
        output.put(DISCORD_BOT_TOKEN, getDiscordToken());
        output.put(USE_SSL_KEY, useSSL());
        output.put(SSL_CERT_FILE_KEY, getSSLCertFile());
        output.put(SSL_PRIVATE_KEY_FILE_KEY, getSSLPrivateKeyFile());
        output.put(SSL_PASSWORD_KEY, getSSLPassword());
        return output;
    }

    public void write(File file) throws IOException {
        FileWriter writer = new FileWriter(file);
        try {
            write().write(writer, 4, 0);
        } finally {
            writer.flush();
            writer.close();
        }
    }

    public static Config read() {
        String configLocation = System.getenv("ROYAL_UR_SERVER_CONFIG");
        if (configLocation == null || configLocation.trim().isEmpty()) {
            logger.log(
                    Level.WARNING,
                    "No config file set via the ROYAL_UR_SERVER_CONFIG env variable. Using the default config."
            );
            return new Config();
        }

        File file = new File(configLocation);
        try {
            return read(file);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read config file " + file, e);
        }
    }

    public static Config read(File file) throws IOException {
        if (file.exists())  {
            try {
                logger.log(Level.INFO, "Reading config from " + file);
                return new Config(readFileToString(file));
            } catch (JSONException e) {
                throw new RuntimeException("Malformed config file " + file, e);
            }
        }

        logger.log(Level.INFO, "Creating default config at " + file);
        Config defaultConfig = new Config();
        defaultConfig.write(file);
        return defaultConfig;
    }

    private static String readFileToString(File file) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, StandardCharsets.UTF_8);
    }
}
