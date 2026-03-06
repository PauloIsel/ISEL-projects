package pt.isel.mpd.mycine_async;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.mycine_async.dto.ActorDto;
import pt.isel.mpd.mycine_async.dto.GenreDto;
import pt.isel.mpd.mycine_async.dto.TvSeriesDto;
import pt.isel.mpd.mycine_utils.requests.async.HttpAsyncRequest;
import pt.isel.mpd.mycine_utils.requests.async.MockAsyncRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoviesDbWebApiTests  {

	@Test
	public void getGenresTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(
				new HttpAsyncRequest()
		);
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockAsyncRequest());
		final int EXPECTED_GENRES = 16;

		List<GenreDto> genres = api.getGenres().join();
		List<GenreDto> genresMock = apiMock.getGenres().join();
		genres.forEach(System.out::println);
		assertEquals(EXPECTED_GENRES, genres.size());
		assertEquals(EXPECTED_GENRES, genresMock.size());
	}
	
	@Test
	public void getAnimationTvSeriesTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpAsyncRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockAsyncRequest());
		int animationGenreId = 16;

		List<TvSeriesDto> series =
			api.searchByGenreIds(1, List.of(animationGenreId)).join();
		List<TvSeriesDto> seriesMock =
			apiMock.searchByGenreIds(1, List.of(animationGenreId)).join();
		series.forEach(System.out::println);

		assertEquals(20, series.size());
		assertEquals(20, seriesMock.size());
	}


	@Test
	public void getActorsOfHomelandTest() {
		int  homelandId = 1407;
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpAsyncRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockAsyncRequest());

		List<ActorDto> actors =
			api.tvSeriesActors(homelandId).join();
		List<ActorDto> actorsMock =
			apiMock.tvSeriesActors(homelandId).join();

		System.out.println(actors.size());
		actors.forEach(System.out::println);
		assertEquals(7, actors.size());
		assertEquals(7, actorsMock.size());
	}

	@Test
	public void getRachelWoodSeriesTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpAsyncRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockAsyncRequest());

		int rachelWoodId = 38940;
		List<TvSeriesDto> series =
			api.actorSeries(rachelWoodId).join();
		List<TvSeriesDto> seriesMock =
			apiMock.actorSeries(rachelWoodId).join();

		for(var m : series)
			System.out.println(m);
		assertEquals(28, series.size());
		assertEquals(28, seriesMock.size());
	}
	
	@Test
	public void recommendationsOfBreakingBadFisrtPageTest() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpAsyncRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockAsyncRequest());
		int BREAKING_BAD_ID= 1396;
		int RECOMMENDATIONS_SIZE=20;
		
		List<TvSeriesDto> series = api
								   .getTvSeriesRecommendations(1, BREAKING_BAD_ID)
								   .join();

		List<TvSeriesDto> seriesMock = apiMock
									   .getTvSeriesRecommendations(1, BREAKING_BAD_ID)
									   .join();
		
		System.out.println("Recomendations size: " + series.size());
		for(var m : series)
			System.out.println(m);
		assertEquals(RECOMMENDATIONS_SIZE, series.size());
		assertEquals(RECOMMENDATIONS_SIZE, seriesMock.size());
	}
	
	@Test
	public void getFirstPageOfTvSeriesNamedHomeLand() {
		MoviesDbWebApi api = new MoviesDbWebApi(new HttpAsyncRequest());
		MoviesDbWebApi apiMock = new MoviesDbWebApi(new MockAsyncRequest());

		List<TvSeriesDto> series = api
								   .searchByName(1,"Homeland")
								   .join();

		List<TvSeriesDto> seriesMock = apiMock
									   .searchByName(1,"Homeland")
									   .join();

		for(var m : series)
			System.out.println(m);
		assertEquals(18, series.size());
		assertEquals(18, seriesMock.size());
	}
}
