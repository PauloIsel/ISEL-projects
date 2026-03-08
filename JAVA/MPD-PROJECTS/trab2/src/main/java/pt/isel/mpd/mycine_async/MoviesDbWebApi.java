package pt.isel.mpd.mycine_async;

import com.google.gson.Gson;
import pt.isel.mpd.mycine_async.dto.*;
import pt.isel.mpd.mycine_utils.requests.async.AsyncRequest;
import pt.isel.mpd.mycine_utils.resources.ResourceUtils;

import java.io.BufferedReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class MoviesDbWebApi {
	public final static int API_PAGE_SIZE=20;
	private static final String API_KEY = getApiKeyFromResources();
	private static final String MOVIES_DB_ENDPOINT =  "https://api.themoviedb.org/3/";
 
 
	private static final String TV_SERIES_GENRES =
		MOVIES_DB_ENDPOINT + "genre/tv/list?api_key=" + API_KEY;
 	
	private static final String TV_SERIES_RECOMMENDATIONS =
		MOVIES_DB_ENDPOINT + "tv/%d/recommendations?page=%d&api_key=" + API_KEY;

	private static final String TV_SERIES_SEARCH_BY_GENRE =
			MOVIES_DB_ENDPOINT + "discover/tv?with_genres=%s&page=%d&api_key=" + API_KEY;

	private static final String TV_SERIES_SEARCH_BY_NAME =
		MOVIES_DB_ENDPOINT + "search/tv?query=%s&page=%d&api_key=" + API_KEY;

	private static final String TV_SERIES_CREDITS =
		MOVIES_DB_ENDPOINT + "tv/%d/credits?api_key="+ API_KEY;

	private static final String TV_SERIES_ACTOR =
			MOVIES_DB_ENDPOINT + "person/%d/tv_credits?api_key=" + API_KEY;


	protected final Gson gson;
	private final AsyncRequest req;

	/**
	 * Retrieve API-KEY from resources
	 * @return
	 */
	private static String getApiKeyFromResources() {
		try {
			var bufReader = new BufferedReader(ResourceUtils.openResource("movies_db_api_key.txt"));
			return bufReader.readLine();
		} catch(Exception e) {
			throw new IllegalStateException(
				"YOU MUST GET a KEY from themoviedb.org and place it in src/main/resources/movies_db_api_key.txt");
		}
	}

	public CompletableFuture<List<GenreDto>> getGenres() {
		String path = TV_SERIES_GENRES;
		return req.getAsync(path)
			.thenApply(reader -> gson.fromJson(reader, GenreListQuery.class).getGenres());
	}
	
	
	public CompletableFuture<List<TvSeriesDto>> getTvSeriesRecommendations(int page, int seriesId) {
		String path = String.format(TV_SERIES_RECOMMENDATIONS, seriesId, page);
		return req.getAsync(path)
				.thenApply(reader -> gson.fromJson(reader, TvSeriesQueryDto.class).getResults());
	}

	public CompletableFuture<List<TvSeriesDto>> searchByGenreIds(int page, List<Integer> genreIds) {
		StringBuilder genreIdsParam = new StringBuilder();
		for (int i = 0; i < genreIds.size(); i++) {
			genreIdsParam.append(genreIds.get(i));
			if (i < genreIds.size() - 1) {
				genreIdsParam.append(",");
			}
		}

		String path = String.format(TV_SERIES_SEARCH_BY_GENRE, genreIdsParam, page);
		return req.getAsync(path)
				.thenApply(reader ->  gson.fromJson(reader, TvSeriesQueryDto.class).getResults());
	}

	public CompletableFuture<List<TvSeriesDto>> searchByName(int page, String nameMatch) {
		String path = String.format(TV_SERIES_SEARCH_BY_NAME, nameMatch, page);
		return req.getAsync(path)
				.thenApply(reader -> gson.fromJson(reader, SearchTvSeriesDto.class).getResults());
	}

	public CompletableFuture<List<ActorDto>> tvSeriesActors(int tvSeriesId) {
		String path = String.format(TV_SERIES_CREDITS, tvSeriesId);
		return req.getAsync(path)
				.thenApply((reader -> gson.fromJson(reader, GetActorsDto.class).getCast()));
	}

	public CompletableFuture<List<TvSeriesDto>> actorSeries(int actorId) {
		String path = String.format(TV_SERIES_ACTOR, actorId);
		return req.getAsync(path)
				.thenApply(reader -> gson.fromJson(reader, GetActorTvSeriesDto.class).getCast());
	}

	public MoviesDbWebApi(AsyncRequest req) {
		this.req = req;
		gson = new Gson();
	}
}
