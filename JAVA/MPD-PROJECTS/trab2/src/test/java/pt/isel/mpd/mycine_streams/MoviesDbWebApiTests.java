package pt.isel.mpd.mycine_streams;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.mycine_utils.requests.HttpRequest;
import java.util.List;
import pt.isel.mpd.mycine_streams.dto.*;
import pt.isel.mpd.mycine_utils.requests.MockRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesDbWebApiTests  {

	@Test
	public void getGenresTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockRequest());
		final int EXPECTED_GENRES = 16;

		List<GenreDto> genres = api.getGenres();
		List<GenreDto> genresMock = apiMock.getGenres();

		genres.forEach(System.out::println);
		assertEquals(EXPECTED_GENRES, genres.size());
		assertEquals(EXPECTED_GENRES, genresMock.size());
	}

	@Test
	public void getAnimationTvSeriesTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockRequest());
		int animationGenreId = 16;

		List<TvSeriesDto> series = api.searchByGenreIds(1, List.of(animationGenreId));
		List<TvSeriesDto> seriesMock = apiMock.searchByGenreIds(1, List.of(animationGenreId));

		series.forEach(System.out::println);
		assertEquals(20, series.size());
		assertEquals(20, seriesMock.size());
	}

	@Test
	public void getActorsOfHomelandTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockRequest());
		int  homelandId = 1407;

		List<ActorDto> actors = api.tvSeriesActors(homelandId);
		List<ActorDto> actorsMock = apiMock.tvSeriesActors(homelandId);

		System.out.println(actors.size());
		actors.forEach(System.out::println);

		assertEquals(7, actors.size());
		assertEquals(7, actorsMock.size());
	}

	@Test
	public void getRachelWoodSeriesTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockRequest());
		int rachelWoodId = 38940;

		List<TvSeriesDto> series = api.actorSeries(rachelWoodId);
		List<TvSeriesDto> seriesMock = apiMock.actorSeries(rachelWoodId);

		for(var m : series)
			System.out.println(m);

		assertEquals(28, series.size());
		assertEquals(28, seriesMock.size());
	}
	
	@Test
	public void recommendationsOfBreakingBadFisrtPageTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockRequest());
		int BREAKING_BAD_ID= 1396;
		int RECOMMENDATIONS_SIZE=20;
		
		List<TvSeriesDto> series = api.getTvSeriesRecommendations(1, BREAKING_BAD_ID);
		List<TvSeriesDto> seriesMock = apiMock.getTvSeriesRecommendations(1, BREAKING_BAD_ID);
		
		System.out.println("Recomendations size: " + series.size());
		for(var m : series)
			System.out.println(m);

		assertEquals(RECOMMENDATIONS_SIZE, series.size());
		assertEquals(RECOMMENDATIONS_SIZE, seriesMock.size());
	}
	
	@Test
	public void getFirstPageOfTvSeriesNamedHomeLand() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockRequest());

		List<TvSeriesDto> series = api.searchByName(1,"Homeland");
		List<TvSeriesDto> seriesMock = apiMock.searchByName(1,"Homeland");

		for(var m : series)
			System.out.println(m);

		assertEquals(18, series.size());
		assertEquals(18, seriesMock.size());
	}
}
