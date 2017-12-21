package me.nikl.lmgtfy;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * Created by nikl on 19.12.17.
 *
 */
public class LmgtfyCommand implements CommandExecutor {

    private Main plugin;
    private Language lang;
    private final String clickCommand = UUID.randomUUID().toString();
    private Shortener shortener;

    public LmgtfyCommand(Main plugin){
        this.plugin = plugin;
        this.lang = plugin.getLang();

        this.shortener = new Shortener(plugin, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("lmgtfy.use")){
            sender.sendMessage(lang.PREFIX + lang.CMD_NO_PERM);
            return true;
        }

        if(args == null || args.length == 0){
            sender.sendMessage(lang.PREFIX + lang.CMD_MISSING_QUERY);
            return true;
        }

        // handle click on the click action...
        //    this will send the link in the chat as the issuing player
        if(args.length == 2 && args[0].equals(clickCommand)){
            if(!(sender instanceof Player)){
                // cannot happen
                return true;
            }
            ((Player) sender).chat(lang.CHAT_MESSAGE.replace("%link%", args[1]));
            return true;
        }

        String query = String.join(" ", args);

        String url;
        try {
            url = "https://lmgtfy.com/?q=" + URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            sender.sendMessage(lang.PREFIX + " Failed to create valid url...");
            return true;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage(lang.PREFIX + " " + url);
            return true;
        }

        if(Main.useShortener) {
            shortener.shortenAsync(url, new Shortener.Callable<String>() {
                // called async!
                @Override
                public void success(String s) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_SHORTENED_SUCCESS);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                            , "tellraw " + createJSON(s, sender.getName()));
                }

                // called async!
                @Override
                public void fail(String s) {
                    sender.sendMessage(lang.PREFIX + lang.CMD_SHORTENED_FAILED);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                            , "tellraw " + createJSON(s, sender.getName()));
                }
            });
        } else {
            sender.sendMessage(lang.PREFIX + lang.CMD_SUCCESS);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender()
                    , "tellraw " + createJSON(url, sender.getName()));
        }
        return true;
    }

    private String createJSON(String url, String name){
        boolean boldClick = true;

        String secondClick = "{\"text\":\"" + lang.CMD_MESSAGE_CLICK_TEXT_2.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_CLICK_COLOR_2 + "\",\"bold\":" + boldClick + ",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url
                + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"" + lang.CMD_MESSAGE_HOVER_TEXT_2.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_HOVER_COLOR_2 +"\"}}},";

        return name
                + " [{\"text\":\"" + lang.JSON_PREFIX_PRE_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.JSON_PREFIX_PRE_COLOR + "\"},{\"text\":\"" + lang.JSON_PREFIX_TEXT + "\",\"color\":\""
                + lang.JSON_PREFIX_COLOR + "\"},{\"text\":\"" + lang.JSON_PREFIX_AFTER_TEXT + "\",\"color\":\""
                + lang.JSON_PREFIX_AFTER_COLOR + "\"}"
                + ",{\"text\":\"" + lang.CMD_MESSAGE_PRE_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_PRE_COLOR + "\"},{\"text\":\""
                + lang.CMD_MESSAGE_CLICK_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_CLICK_COLOR + "\",\"bold\":" + boldClick
                + ",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/lmgtfy "
                + clickCommand + " " + url
                + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\""
                + lang.CMD_MESSAGE_HOVER_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_HOVER_COLOR + "\"}}}, " + secondClick +  " {\"text\":\""
                + lang.CMD_MESSAGE_AFTER_TEXT.replace("%link%", url) + "\",\"color\":\""
                + lang.CMD_MESSAGE_AFTER_COLOR + "\"}]";
    }
}