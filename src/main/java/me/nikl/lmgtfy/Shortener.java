package me.nikl.lmgtfy;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by nikl on 19.12.17.
 *
 */
public class Shortener {
    private LmgtfyCommand lmgtfyCommand;
    private Main plugin;

    public Shortener(Main plugin, LmgtfyCommand lmgtfyCommand){
        this.lmgtfyCommand = lmgtfyCommand;
        this.plugin = plugin;
    }

    public void shortenAsync(String link, Callable<String> callable){
        new Lookup(plugin, link, callable).runTaskAsynchronously(plugin);
    }


    public interface Callable<T>{
        void success(T t);
        void fail(T t);
    }

    private class Lookup extends BukkitRunnable{
        private final static String REQ = "https://is.gd/create.php?format=simple&url=";
        private String link;
        private Callable<String> callable;
        private Main plugin;

        public Lookup(Main plugin, String link, Callable<String> callable){
            this.link = link;
            this.callable = callable;
            this.plugin = plugin;
        }

        @Override
        public void run() {
            try {
                final String shortLink = shorten(link);
                callable.success(shortLink);
            } catch (IOException e) {
                callable.fail(link);
            }
        }

        private String shorten(String longUrl) throws IOException {
            Bukkit.getLogger().info("got: " + longUrl);
            String isgdUrlLookup = REQ + URLEncoder.encode(longUrl, "UTF-8");
            BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(isgdUrlLookup).openStream()));
            String isgdUrl = reader.readLine();

            Bukkit.getLogger().info("short: " + isgdUrl);
            return isgdUrl;
        }
    }
}