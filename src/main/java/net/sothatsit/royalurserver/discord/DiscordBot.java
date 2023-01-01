package net.sothatsit.royalurserver.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.MiscUtil;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.sothatsit.royalurserver.game.Game;
import net.sothatsit.royalurserver.game.GameID;
import net.sothatsit.royalurserver.game.GameListener;
import net.sothatsit.royalurserver.game.PlayerState;
import net.sothatsit.royalurserver.management.GameManager;
import net.sothatsit.royalurserver.management.MatchMaker;
import net.sothatsit.royalurserver.network.Client;

import javax.security.auth.login.LoginException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * A discord bot for RoyalUr.net that allows the creation
 * of games directly through the Discord.
 */
public class DiscordBot extends ListenerAdapter implements GameListener {

    private static final String URL = "https://royalur.net";

    private final JDA jda;
    private final MatchMaker matchMaker;
    private final GameManager gameManager;

    public DiscordBot(String token, MatchMaker matchMaker, GameManager gameManager) throws LoginException {
        this.matchMaker = matchMaker;
        this.gameManager = gameManager;
        this.jda = JDABuilder.createDefault(token)
                .disableCache(
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.VOICE_STATE,
                        CacheFlag.ACTIVITY
                ).build();
        this.jda.addEventListener(this);
    }

    public void shutdown() {
        jda.shutdown();
    }

    private String encodeQueryParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding name parameter", e);
        }
    }

    private String generateGameURL(GameID gameID, String name) {
        return "<" + URL + "/game/"
                + (name != null ? "?name=" + encodeQueryParam(name) : "")
                + "#" + gameID + ">";
    }

    private RestAction<User> parseMention(String mention) {
        Matcher matcher = Message.MentionType.USER.getPattern().matcher(mention);
        if (!matcher.matches())
            return null;

        long id = MiscUtil.parseSnowflake(matcher.group(1));
        return jda.retrieveUserById(id);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        String raw = message.getContentRaw();
        if (raw.isEmpty() || raw.charAt(0) != '!')
            return;

        String[] command = raw.substring(1).split("\\s+");
        if (command[0].equalsIgnoreCase("website")) {
            channel.sendMessage("<" + URL + ">").queue();
            return;
        }

        if (command[0].equalsIgnoreCase("new-game") || command[0].equalsIgnoreCase("new-games")) {
            if (command.length > 1 && (command.length - 1) % 2 == 0) {
                int games = (command.length - 1) / 2;
                StringBuilder reply = new StringBuilder();
                reply.append("Generated game").append(games > 1 ? "s" : "").append("!\n\n");

                for (int game = 0; game < games; ++game) {
                    GameID gameID = matchMaker.generateGame();
                    String name1 = Client.sanitiseName(command[2 * game + 1]);
                    String name2 = Client.sanitiseName(command[2 * game + 2]);

                    reply.append("**").append(name1).append(":** ");
                    reply.append(generateGameURL(gameID, name1)).append("\n");
                    reply.append("**").append(name2).append(":** ");
                    reply.append(generateGameURL(gameID, name2)).append("\n\n");
                }

                channel.sendMessage(reply).queue();
                return;
            } else if (command.length == 1) {
                GameID game = matchMaker.generateGame();

                StringBuilder reply = new StringBuilder();
                reply.append("Generated new game!\n\n");
                reply.append("**Join here:** ").append(generateGameURL(game, null));
                channel.sendMessage(reply).queue();
                return;
            } else {
                channel.sendMessage("**Usage:** !new-games ([player-one player-two], ...)").queue();
                return;
            }
        }

        if (command[0].equalsIgnoreCase("pm-game") || command[0].equalsIgnoreCase("pm-games")) {
            if (command.length == 1 || (command.length - 1) % 2 != 0) {
                channel.sendMessage("**Usage:** !pm-games ([@PlayerOne @PlayerTwo], ...)").queue();
                return;
            }

            int games = (command.length - 1) / 2;
            List<RestAction<User>> userActions = new ArrayList<>(2 * games);
            boolean anyInvalidMentions = false;

            for (int game = 0; game < games; ++game) {
                String user1Mention = command[2 * game + 1];
                String user2Mention = command[2 * game + 2];
                RestAction<User> user1Action = parseMention(user1Mention);
                RestAction<User> user2Action = parseMention(user2Mention);
                if (user1Action == null || user2Action == null) {
                    anyInvalidMentions = true;
                    break;
                }
                userActions.add(user1Action);
                userActions.add(user2Action);
            }
            if (anyInvalidMentions) {
                channel.sendMessage("Please mention users, instead of typing their names").queue();
                return;
            }

            RestAction.allOf(userActions).queue(users -> {
                for (int index = 0; index < users.size(); ++index) {
                    User user = users.get(index);
                    if (user == null) {
                        channel.sendMessage("Unable to find user " + command[index + 1]).queue();
                        return;
                    }
                }

                for (int game = 0; game < games; ++game) {
                    User user1 = users.get(2 * game);
                    User user2 = users.get(2 * game + 1);
                    createAndPMGame(user1, user2);
                }

                channel.sendMessage("Sent private messages with game links!").queue();
            });
            return;
        }

        if (command[0].equalsIgnoreCase("active-games")) {
            List<Game> games = gameManager.getActiveGames();
            if (games.isEmpty()) {
                channel.sendMessage("There are no active games.").queue();
                return;
            }

            StringBuilder response = new StringBuilder();
            for (Game game : games) {
                response.append("**").append(game.light.name).append("**");
                response.append(" vs. ");
                response.append("**").append(game.dark.name).append("**: ");
                response.append(generateGameURL(game.id, null)).append("\n");
            }
            channel.sendMessage(response).queue();
            return;
        }

        if (command[0].equalsIgnoreCase("challenge")) {
            if (command.length != 2) {
                channel.sendMessage("**Usage:** !challenge @OtherPlayer").queue();
                return;
            }

            RestAction<User> otherUserAction = parseMention(command[1]);
            if (otherUserAction == null) {
                channel.sendMessage("Please @mention the user you wish to challenge.").queue();
                return;
            }

            otherUserAction.queue(otherUser -> {
                User user = event.getAuthor();
                createAndPMGame(user, otherUser);
                channel.sendMessage("Sent private messages with game links!").queue();
            });
        }
    }

    private void createAndPMGame(User user1, User user2) {
        GameID gameID = matchMaker.generateGame();
        String name1 = Client.sanitiseName(user1.getName());
        String name2 = Client.sanitiseName(user2.getName());

        String message1 = "Here is your game link to play against "
                + "**" + name2 + "**!\n\n" + generateGameURL(gameID, name1);
        String message2 = "Here is your game link to play against "
                + "**" + name1 + "**!\n\n" + generateGameURL(gameID, name2);

        // Send private messages to users.
        user1.openPrivateChannel().queue(pm -> {
            pm.sendMessage(message1).queue();
        });
        user2.openPrivateChannel().queue(pm -> {
            pm.sendMessage(message2).queue();
        });
    }

    private boolean isUnknownName(String name) {
        return "unknown".equalsIgnoreCase(name) || name.trim().isEmpty();
    }

    @Override
    public void onGameWin(Game game) {
        TextChannel channel = jda.getTextChannelById(842998018575433738L);
        if (channel == null)
            return;

        PlayerState winner = game.getWinner();
        PlayerState loser = game.getLoser();
        if (isUnknownName(winner.name) && isUnknownName(loser.name))
            return;

        String winnerName = (isUnknownName(winner.name) ? "An unknown player" : winner.name);
        String loserName = (isUnknownName(loser.name) ? "an unknown player" : loser.name);

        String message = "**" + winnerName + "** just defeated **" + loserName + "**";
        message += " as " + winner.player.name;
        message += " with a score of " + winner.getScore() + " to " + loser.getScore() + "!";
        channel.sendMessage(message).queue();
    }
}
