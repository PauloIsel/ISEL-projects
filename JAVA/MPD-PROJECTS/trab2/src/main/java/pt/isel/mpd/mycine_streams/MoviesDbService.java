package pt.isel.mpd.mycine_streams;

import pt.isel.mpd.mycine_streams.dto.*;
import pt.isel.mpd.mycine_streams.model.*;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import static pt.isel.mpd.mycine_utils.streams.StreamUtils.sortedIntersection;

public class MoviesDbService {

	private MoviesDbWebApi api;

	public Stream<TvSeries> searchByGenre(int maxSeries, List<Integer> genreIds) {
		return Stream.iterate(1, i -> i + 1)
				.map(page -> api.searchByGenreIds(page, genreIds))
				.takeWhile(list -> !list.isEmpty())
				.flatMap(List::stream)
				.limit(maxSeries)
				.map(this::dtoToTvSeries);
	}

	public Stream<TvSeries> searchByName(int maxSeries, String match ) {
		return Stream.iterate(1, i -> i + 1)
				.map(page -> api.searchByName(page, match))
				.takeWhile(list -> !list.isEmpty())
				.flatMap(List::stream)
				.limit(maxSeries)
				.map(this::dtoToTvSeries);
	}

	public Stream<Genre> getGenres() {
		return Stream.of(1)
				.flatMap(i -> api.getGenres().stream().map(this::dtoToGenre));
	}

	public Stream<Genre> getGenres(List<Integer> genreIds) {
		return Stream.of(1)
				.flatMap(i -> getGenres())
				.filter(g -> genreIds.contains(g.getId()));
	}

	public Stream<TvSeries> getCommonRecommendations(int seriesId1, int seriesId2) {
		var cmp = Comparator.comparing(TvSeries::getId);
		var s1 = getTvSeriesRecommendations(60, seriesId1).sorted(cmp);
		var s2 = getTvSeriesRecommendations(60, seriesId2).sorted(cmp);
		return sortedIntersection(cmp, s1, s2);
	}

	public Stream<TvSeries> getTvSeriesRecommendations(int maxSeries, int tvSeriesId) {
		return Stream.iterate(1, i -> i + 1)
				.map(page -> api.getTvSeriesRecommendations(page, tvSeriesId))
				.takeWhile(list -> !list.isEmpty())
				.flatMap(List::stream)
				.limit(maxSeries)
				.map(this::dtoToTvSeries);
	}

	public Stream<Actor> getTvSeriesActors(int tvSeriesId) {
		return Stream.of(1)
				.flatMap(i -> api.tvSeriesActors(tvSeriesId).stream())
				.map(this::dtoToActor)
				.filter( t -> tvSeriesId != t.getId());
	}
	
	public Stream<TvSeries> getActorTvSeries(int actorId) {
		return Stream.of(1)
				.flatMap(i -> api.actorSeries(actorId).stream())
				.map(this::dtoToTvSeries)
				.filter( a -> actorId != a.getId());
	}

	private TvSeries dtoToTvSeries(TvSeriesDto dto) {
		Function<Integer, Stream<Genre>> genreFunction =
				maxS -> getGenres(dto.getGenreIds());
		Function<Integer, Stream<TvSeries>> recommendationFunction =
				maxS -> getTvSeriesRecommendations(maxS, dto.getId());
		return new TvSeries(
			dto.getStartDate(),
			dto.getName(),
			dto.getId(),
			dto.getPopularity(),
			dto.getNetworks(),
			getTvSeriesActors(dto.getId()),
			genreFunction,

			// Altere este código para
			// poder especificar o máximo de séries na
			// chamada ao mét0do getSeries de Genre
			recommendationFunction);
	}

	

	private Genre dtoToGenre(GenreDto dto) {
		Function<Integer, Stream<TvSeries>> function =
				maxSeries -> searchByGenre(maxSeries, List.of(dto.getId()));
	    return new Genre(dto.getId(),
		                 dto.getName(),
						 // Altere este código para
						 // poder especificar o máximo de séries na
						 // chamada ao método getSeries de Genre
		                 function);
	}

	private Actor dtoToActor(ActorDto dto) {
		return new Actor(dto.getId(),
						dto.getName(),
						dto.getPopularity(),
						getActorTvSeries(dto.getId()));
	}

	public MoviesDbService(MoviesDbWebApi api) {
		this.api = api;
	}
}
