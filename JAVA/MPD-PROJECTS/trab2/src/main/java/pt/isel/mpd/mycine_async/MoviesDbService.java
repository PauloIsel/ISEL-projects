package pt.isel.mpd.mycine_async;

import pt.isel.mpd.mycine_async.dto.ActorDto;
import pt.isel.mpd.mycine_async.dto.GenreDto;
import pt.isel.mpd.mycine_async.dto.TvSeriesDto;
import pt.isel.mpd.mycine_async.model.Actor;
import pt.isel.mpd.mycine_async.model.Genre;
import pt.isel.mpd.mycine_async.model.TvSeries;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

public class MoviesDbService {

	private MoviesDbWebApi api;

	/**
	 * get 2 pages in parallel starting from firstPage
	 * @param startPage
	 * @param genreIds
	 * @return
	 */
	public CompletableFuture<List<TvSeries>> searchByGenreParallel(int startPage, List<Integer> genreIds) {
		CompletableFuture<List<TvSeriesDto>> page1 = api.searchByGenreIds(startPage, genreIds);
		CompletableFuture<List<TvSeriesDto>> page2 = api.searchByGenreIds(startPage + 1, genreIds);

		return page1.thenCombine(page2, (list1, list2) -> {
			list1.addAll(list2);
			return list1.stream()
					.map(this::dtoToTvSeries)
					.toList();
		});
	}
	
	public CompletableFuture<Stream<TvSeries>> searchByNameParallel(int maxSeries, String match ) {
		CompletableFuture<List<TvSeriesDto>> page1 = api.searchByName(1, match);
		CompletableFuture<List<TvSeriesDto>> page2 = api.searchByName(2, match);

		return page1.thenCombine(page2, (list1, list2) -> {
			list1.addAll(list2);
			return list1.stream()
					.map(this::dtoToTvSeries)
					.limit(maxSeries);
		});
	}
	
	public CompletableFuture<Stream<TvSeries>> searchByGenre(int maxSeries, List<Integer> genreIds) {
		return searchByGenreParallel(1, genreIds)
				.thenApply(list -> list.stream().limit(maxSeries));
	}

	public CompletableFuture<Stream<TvSeries>> searchByName(int maxSeries, String match ) {
		return searchByNameParallel(maxSeries, match)
				.thenApply(stream -> stream.limit(maxSeries));
	}
	
	public CompletableFuture<Stream<TvSeries>> getTvSeriesRecommendations(int maxSeries, int tvSeriesId) {
		return api.getTvSeriesRecommendations(1, tvSeriesId)
				.thenApply(list -> list.stream()
						.map(this::dtoToTvSeries)
						.limit(maxSeries));
	}

	public CompletableFuture<Stream<Genre>> getGenres() {
		return api.getGenres()
				.thenApply(list -> list.stream()
						.map(this::dtoToGenre));
	}

	public CompletableFuture<Stream<Genre>> getGenres(List<Integer> genreIds) {
		return getGenres()
				.thenApply(stream -> stream.filter(g -> genreIds.contains(g.getId())));
	}

	public CompletableFuture<Stream<Actor>> getTvSeriesActors(int tvSeriesId) {
		return api.tvSeriesActors(tvSeriesId)
				.thenApply(list -> list.stream()
						.map(this::dtoToActor)
						.filter(t -> tvSeriesId != t.getId()));
	}
	
	public CompletableFuture<Stream<TvSeries>> getActorTvSeries(int actorId) {
		return api.actorSeries(actorId)
				.thenApply(list -> list.stream()
						.map(this::dtoToTvSeries)
						.filter(a -> actorId != a.getId()));
	}
	
	private TvSeries dtoToTvSeries(TvSeriesDto dto) {
		Function<Integer, CompletableFuture<Stream<Genre>>> genreFunction =
				maxSeries -> getGenres(dto.getGenreIds());
		Function<Integer, CompletableFuture<Stream<TvSeries>>> recommendationsFunction =
				maxSeries -> getTvSeriesRecommendations(maxSeries, dto.getId());

		return new TvSeries(
			dto.getStartDate(),
			dto.getName(),
			dto.getId(),
			dto.getPopularity(),
			dto.getNetworks(),
			() -> getTvSeriesActors(dto.getId()),
			genreFunction,
			recommendationsFunction);
	}
	
	private Genre dtoToGenre(GenreDto dto) {
		Function<Integer, CompletableFuture<Stream<TvSeries>>> seriesFunction =
				maxSeries -> searchByGenre(maxSeries, List.of(dto.getId()));

	    return new Genre(dto.getId(),
		                 dto.getName(),
						seriesFunction);
	}

	private Actor dtoToActor(ActorDto dto) {
		return new Actor(dto.getId(),
						dto.getName(),
						dto.getPopularity(),
						() -> getActorTvSeries(dto.getId()));
	}

	public MoviesDbService(MoviesDbWebApi api) {
		this.api = api;
	}
}
