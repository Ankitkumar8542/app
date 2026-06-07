package com.musicPlayer.app.common.constants;


public final class AppConstants {
    private AppConstants() {}

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final String DEFAULT_SORT = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_PREMIUM = "ROLE_PREMIUM";

    public static final String CACHE_SONGS = "songs";
    public static final String CACHE_ARTISTS = "artists";
    public static final String CACHE_ALBUMS = "albums";
    public static final String CACHE_PLAYLISTS = "playlists";
    public static final String CACHE_CATEGORIES = "categories";

    public static final String CLOUDINARY_SONGS_FOLDER = "music_player/songs";
    public static final String CLOUDINARY_IMAGES_FOLDER = "music_player/images";
    public static final String CLOUDINARY_AVATARS_FOLDER = "music_player/avatars";

    public static final long FREE_MONTHLY_PLAYS = 100;
    public static final long MAX_FREE_PLAYLIST_SONGS = 25;
    public static final long MAX_FREE_PLAYLISTS = 3;
}