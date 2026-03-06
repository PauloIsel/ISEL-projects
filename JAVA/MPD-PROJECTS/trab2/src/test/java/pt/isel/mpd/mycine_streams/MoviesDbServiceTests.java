package pt.isel.mpd.mycine_streams;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.mycine_streams.model.Genre;
import pt.isel.mpd.mycine_streams.model.TvSeries;
import pt.isel.mpd.mycine_utils.requests.CounterRequest;
import pt.isel.mpd.mycine_utils.requests.HttpRequest;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesDbServiceTests {

	@Test
	public void getGenresTest() {
		CounterRequest req = new CounterRequest( new HttpRequest() );
		MoviesDbService serv =
			new MoviesDbService(new MoviesDbWebApi(req));

		Stream<Genre> sgenres = serv.getGenres();
		assertEquals(0, req.getCount());

		List<Genre> genres = sgenres.toList();
		genres.forEach(System.out::println);
		assertEquals(16, genres.size());
		assertEquals(1, req.getCount());
	}

	
	@Test
	public void getCrimeAndOrMisteryTvSeriesTest() {
		CounterRequest req = new CounterRequest( new HttpRequest() );
		int misteryGenreId = 9648;
		int crimeGenreId = 80;
		int maxSeries = 40;

		MoviesDbService serv =
			new MoviesDbService(
				new MoviesDbWebApi(
					req
				)
			);
		
		Stream<TvSeries> series =
			serv.searchByGenre(maxSeries, List.of(misteryGenreId, crimeGenreId) );

		assertEquals(0, req.getCount());
		System.out.println(series.count());

		assertEquals(2, req.getCount());

		Stream<TvSeries> firstTen =
				serv.searchByGenre(maxSeries, List.of(misteryGenreId, crimeGenreId))
				.limit(10);

		assertEquals(2, req.getCount());

		firstTen.forEach(series2 -> {
			System.out.println(series2.getName() + " actors:");
			series2.getActors().forEach(System.out::println);
			System.out.println();
		});
	 
		assertEquals(13, req.getCount());
		
	}

	@Test
	public void getActorsOfWestWorldSeriesTest() {
		int westWorldSeriesId = 63247; // WestWorld, 2020
		CounterRequest req = new CounterRequest(new HttpRequest());
		MoviesDbService serv =
			  new MoviesDbService(new MoviesDbWebApi(req));

		
		var actorsList = serv.getTvSeriesActors(westWorldSeriesId).toList();
		actorsList.forEach(System.out::println);
		assertEquals(1, req.getCount());
		assertEquals(9, actorsList.size());
		assertEquals(1, req.getCount());
	}

 

	@Test
	public void getRachelWoodMoviesTest() {
		CounterRequest req = new CounterRequest(new HttpRequest());
		MoviesDbService serv =
			new MoviesDbService(new MoviesDbWebApi(req));

		int rachelWoodId = 38940;
		Stream<TvSeries> seriesStream =
			serv.getActorTvSeries(rachelWoodId);
		assertEquals(0, req.getCount());

		var seriesList = seriesStream.toList();
		
		seriesList.forEach(System.out::println);
		assertEquals(1, req.getCount());
		assertEquals(28, seriesList.size());
	}

	@Test
	public void searchByNameTest() {
		CounterRequest req = new CounterRequest(new HttpRequest());
		MoviesDbService serv = new MoviesDbService(new MoviesDbWebApi(req));

		int maxSeries = 2;
		String match = "Breaking+Bad";

		Stream<TvSeries> seriesStream = serv.searchByName(maxSeries, match);

		assertEquals(0, req.getCount());

		var seriesList = seriesStream.toList();
		seriesList.forEach(System.out::println);

		assertEquals(1, req.getCount());
		assertEquals(maxSeries, seriesList.size());
	}

	@Test
	public void getTvSeriesRecommendationsTest() {
		int tvSeriesId = 12345;
		int maxSeries = 5;
		CounterRequest req = new CounterRequest(new HttpRequest());
		MoviesDbService serv = new MoviesDbService(new MoviesDbWebApi(req));

		Stream<TvSeries> recommendations = serv.getTvSeriesRecommendations(maxSeries, tvSeriesId);

		assertEquals(0, req.getCount());

		var recommendationsList = recommendations.toList();
		recommendationsList.forEach(System.out::println);

		assertEquals(1, req.getCount());
		assertEquals(maxSeries, recommendationsList.size());
	}

	@Test
	public void getGenresTestWithDifferentMaxSeries() {
		CounterRequest req = new CounterRequest(new HttpRequest());
		MoviesDbService serv = new MoviesDbService(new MoviesDbWebApi(req));

		Stream<Genre> genresStream = serv.getGenres();

		assertEquals(0, req.getCount());

		var genresList = genresStream.toList();
		genresList.forEach(System.out::println);

		assertEquals(1, req.getCount());
		assertEquals(16, genresList.size());
	}

	@Test
	public void getCommonRecommendationsTest() {
		// IDs reais de séries populares (você pode substituir por outros IDs válidos)
		int breakingBadId = 1396;  // Breaking Bad
		int betterCallSaulId = 60059; // Better Call Saul

		CounterRequest req = new CounterRequest(new HttpRequest());
		MoviesDbService serv = new MoviesDbService(new MoviesDbWebApi(req));

		// Obter recomendações comuns
		Stream<TvSeries> commonRecommendations =
				serv.getCommonRecommendations(breakingBadId, betterCallSaulId);

		// Verificar que nenhuma requisição foi feita ainda (lazy evaluation)
		assertEquals(0, req.getCount());

		// Coletar resultados
		List<TvSeries> resultList = commonRecommendations.toList();

		//check if resultList is empty
        assertFalse(resultList.isEmpty());

		// Imprimir resultados
		System.out.println("Recomendações comuns para as séries:");
		resultList.forEach(System.out::println);

		// Verificar que houve requisições à API para ambas as séries
		System.out.println("count: " + req.getCount());

		assertTrue(req.getCount() >= 2);

		// Verificar que os resultados estão ordenados por ID
		if (resultList.size() > 1) {
			for (int i = 1; i < resultList.size(); i++) {
				assertTrue(resultList.get(i-1).getId() <= resultList.get(i).getId());
			}
		}
	}

	@Test
	public void getCommonRecommendationsWithLimitTest() {
		int westWorldId = 63247;
		int strangerThingsId = 66732;
		int limit = 3;

		CounterRequest req = new CounterRequest(new HttpRequest());
		MoviesDbService serv = new MoviesDbService(new MoviesDbWebApi(req));

		// Obter apenas 3 recomendações comuns
		List<TvSeries> limitedResults = serv.getCommonRecommendations(westWorldId, strangerThingsId)
				.limit(limit)
				.toList();

		// Verificar que não excede o limite e não está vazia
		assertTrue(!limitedResults.isEmpty() && limitedResults.size() <= limit);

		// Imprimir os resultados limitados
		System.out.println("Primeiras " + limit + " recomendações comuns:");
		limitedResults.forEach(System.out::println);
	}

	@Test
	public void getCommonRecommendationsNoIntersectionTest() {
		// Séries que provavelmente não têm recomendações em comum
		int documentaryId = 45790;   // Planeta Terra (documentário)
		int animationId = 1434;      // Family Guy (animação)

		CounterRequest req = new CounterRequest(new HttpRequest());
		MoviesDbService serv = new MoviesDbService(new MoviesDbWebApi(req));

		Stream<TvSeries> commonRecommendations =
				serv.getCommonRecommendations(documentaryId, animationId);

		// Se não houver interseção, o resultado deve ser vazio
		// Mas ainda devem ser feitas requisições à API
		List<TvSeries> resultList = commonRecommendations.limit(10).toList();
		System.out.println("count: " + req.getCount());
		assertTrue(req.getCount() > 0);
        assertEquals(0, resultList.size());
	}
}
