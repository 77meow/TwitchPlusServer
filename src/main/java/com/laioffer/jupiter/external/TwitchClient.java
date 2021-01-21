package com.laioffer.jupiter.external;

import com.laioffer.jupiter.entity.Game;
import com.laioffer.jupiter.entity.Item;
import com.laioffer.jupiter.entity.ItemType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class TwitchClient {
    private static final String TOKEN = "Bearer 5823u83ooy0lf7pxppx6vmtjpxb4zk";
    private static final String CLIENT_ID = "whpomaqoru2217cudg5ljunybzajaj";
    private static final String TOP_GAME_URL_TEMPLATE = "https://api.twitch.tv/helix/games/top?first=%s";
    private static final String GAME_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/games?name=%s";
    private static final int DEFAULT_GAME_LIMIT = 20;
    private static final String STREAM_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/streams?game_id=%s&first=%s";
    private static final String VIDEO_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/videos?game_id=%s&first=%s";
    private static final String CLIP_SEARCH_URL_TEMPLATE = "https://api.twitch.tv/helix/clips?game_id=%s&first=%s";
    private static final String TWITCH_BASE_URL = "https://www.twitch.tv/";
    private static final int DEFAULT_SEARCH_LIMIT = 20;

    private String searchTwitch(String url) throws TwitchException {
        CloseableHttpClient httpclient = HttpClients.createDefault();

        ResponseHandler<String> responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status != 200) {
                throw new TwitchException("Failed to get data from Twitch");
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new TwitchException("Failed to get data from Twitch");
            }
            JSONObject obj = new JSONObject(EntityUtils.toString(entity));
            return obj.getJSONArray("data").toString();
        };

        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", TOKEN);
            request.setHeader("Client-ID", CLIENT_ID);
            return httpclient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
            throw new TwitchException("Failed to get data from Twitch");
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Game> getGameList(String data) throws TwitchException {
        // Do conversion here: JSON String -> Java Object
        ObjectMapper mapper = new ObjectMapper();
        try { // JsonProperty is used when converting Json -> Object
            Game[] games = mapper.readValue(data, Game[].class);
            return Arrays.asList(games);
        } catch (JsonProcessingException e) {
            throw new TwitchException("Failed to parse game data from Twitch API");
        }
    }

    private List<Item> searchStreams(String gameId, int limit) throws TwitchException {
        String url = buildSearchURL(STREAM_SEARCH_URL_TEMPLATE, gameId, limit);
        String data = searchTwitch(url);
        List<Item> streams = getItemList(data);
        for (Item item: streams) {
            item.setUrl(DEFAULT_SEARCH_LIMIT + item.getBroadcasterName());
            item.setType(ItemType.STREAM);
        }
        return streams;
    }

    private List<Item> searchClips(String gameId, int limit) throws TwitchException {
        List<Item> clips = getItemList(searchTwitch(buildSearchURL(CLIP_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : clips) {
            item.setType(ItemType.CLIP);
        }
        return clips;
    }

    private List<Item> searchVideos(String gameId, int limit) throws TwitchException {
        List<Item> videos = getItemList(searchTwitch(buildSearchURL(VIDEO_SEARCH_URL_TEMPLATE, gameId, limit)));
        for (Item item : videos) {
            item.setType(ItemType.VIDEO);
        }
        return videos;
    }

    private List<Item> getItemList(String data) throws TwitchException {
        // Do conversion here: JSON String -> Java Object
        ObjectMapper mapper = new ObjectMapper();
        try { // JsonProperty is used when converting Json -> Object
            return Arrays.asList(mapper.readValue(data, Item[].class));
        } catch (JsonProcessingException e) {
            throw new TwitchException("Failed to parse Item data from Twitch API");
        }
    }

    private String buildSearchURL(String url, String gameId, int limit) {
            try {
                // among us -> encode -> among%20us
                gameId = URLEncoder.encode(gameId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // https://api.twitch.tv/helix/streams?game_id=%s&first=%s
            return String.format(url, gameId, limit);
    }

    private String buildGameURL(String url, String gameName, int limit) {
        if (gameName.equals("")) {
            // https://api.twitch.tv/helix/games/top?first=20
            return String.format(url, limit);
        } else {
            try {
                // among us -> encode -> among%20us
                gameName = URLEncoder.encode(gameName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            // https://api.twitch.tv/helix/games/top?game=amog us
            return String.format(url, gameName);
        }
    }

    public List<Game> topGames(int limit) throws TwitchException {
        if (limit <= 0) {
            limit = DEFAULT_GAME_LIMIT;
        }
        String url = buildGameURL(TOP_GAME_URL_TEMPLATE, "", limit);
        String responseBody = searchTwitch(url);
        return getGameList(responseBody);
    }

    public Game searchGame(String gameName) throws TwitchException {
        List<Game> gameList = getGameList(searchTwitch(buildGameURL(GAME_SEARCH_URL_TEMPLATE, gameName, 0)));
        if (gameList.size() != 0) { // Size is supposed to be 1
            return gameList.get(0);
        }
        return null;
    }

    public List<Item> searchByType(String gameId, ItemType type, int limit) throws TwitchException {
        List<Item> items = Collections.emptyList();

        switch (type) {
            case STREAM:
                items = searchStreams(gameId, limit);
                break;
            case VIDEO:
                items = searchVideos(gameId, limit);
                break;
            case CLIP:
                items = searchClips(gameId, limit);
                break;
        }

        // Update gameId for all items. GameId is used by recommendation function
        for (Item item : items) {
            item.setGameId(gameId);
        }
        return items;
    }

    public Map<String, List<Item>> searchItems(String gameId) throws TwitchException {
        Map<String, List<Item>> itemMap = new HashMap<>();
        for (ItemType type : ItemType.values()) {
            itemMap.put(type.toString(), searchByType(gameId, type, DEFAULT_SEARCH_LIMIT));
        }
        return itemMap;
    }

}