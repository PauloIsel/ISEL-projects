package pt.isel.mpd.mycine_streams;

import com.google.gson.Gson;
import pt.isel.mpd.mycine_streams.dto.*;
import pt.isel.mpd.mycine_utils.requests.Request;
import pt.isel.mpd.mycine_utils.resources.ResourceUtils;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.List;


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
	private final Request req;

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

	public List<GenreDto> getGenres() {
		String path = TV_SERIES_GENRES;
		Reader reader = req.get(path);
		GenreListQuery genresQuery =
			gson.fromJson(reader, GenreListQuery.class);
		return genresQuery.getGenres();
	}
	
	
	public List<TvSeriesDto> getTvSeriesRecommendations(int page, int seriesId) {
		String path = String.format(TV_SERIES_RECOMMENDATIONS, seriesId, page);
		Reader reader = req.get(path);
		TvSeriesQueryDto recommendations =
			gson.fromJson(reader, TvSeriesQueryDto.class);
		return recommendations.getResults();
	}

	public List<TvSeriesDto> searchByGenreIds(int page, List<Integer> genreIds) {
		StringBuilder genreIdsParam = new StringBuilder();
		for (int i = 0; i < genreIds.size(); i++) {
			genreIdsParam.append(genreIds.get(i));
			if (i < genreIds.size() - 1) {
				genreIdsParam.append(",");
			}
		}

		String path = String.format(TV_SERIES_SEARCH_BY_GENRE, genreIdsParam, page);
		Reader reader = req.get(path);
		TvSeriesQueryDto series = gson.fromJson(reader, TvSeriesQueryDto.class);
		return series.getResults();
	}

	public List<TvSeriesDto> searchByName(int page, String nameMatch) {
		String path =  String.format(TV_SERIES_SEARCH_BY_NAME, nameMatch, page);
		Reader reader = req.get(path);
		SearchTvSeriesDto series =
			gson.fromJson(reader, SearchTvSeriesDto.class);
		return series.getResults();
	}

	public List<ActorDto> tvSeriesActors(int tvSeriesId) {
		String path =  String.format(TV_SERIES_CREDITS, tvSeriesId);
		Reader reader = req.get(path);

		GetActorsDto credits =
			gson.fromJson(reader, GetActorsDto.class);
		return credits.getCast();

	}

	public List<TvSeriesDto> actorSeries(int actorId) {
		String path =  String.format(TV_SERIES_ACTOR, actorId);
		Reader reader = req.get(path);
		GetActorTvSeriesDto actor =
			gson.fromJson(reader, GetActorTvSeriesDto.class);
		return actor.getCast();
	}

	public MoviesDbWebApi(Request req) {
		this.req = req;
		gson = new Gson();
	}
}
