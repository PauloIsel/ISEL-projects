package pt.isel.mpd.mycine_streams;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.mycine_streams.dto.ActorDto;
import pt.isel.mpd.mycine_streams.dto.GenreDto;
import pt.isel.mpd.mycine_streams.dto.TvSeriesDto;
import pt.isel.mpd.mycine_utils.requests.HttpRequest;
import pt.isel.mpd.mycine_utils.requests.SaverRequest;

import java.util.List;

/**
 * Classe de testes para popular a cache com respostas da API.
 */
public class MoviesDbWebApiCacheSaverTests {

    @Test
    public void saveGenresToCache() {
        MoviesDbWebApi api = new MoviesDbWebApi(
                new SaverRequest(new HttpRequest())
        );

        List<GenreDto> genres = api.getGenres();
        genres.forEach(System.out::println);
    }

    @Test
    public void saveAnimationTvSeriesToCache() {
        MoviesDbWebApi api = new MoviesDbWebApi(
                new SaverRequest(new HttpRequest())
        );
        int animationGenreId = 16;
        List<TvSeriesDto> series =
                api.searchByGenreIds(1, List.of(animationGenreId));
        series.forEach(System.out::println);
    }

    @Test
    public void saveActorsOfHomelandToCache() {
        int homelandId = 1407;
        MoviesDbWebApi api = new MoviesDbWebApi(
                new SaverRequest(new HttpRequest())
        );

        List<ActorDto> actors = api.tvSeriesActors(homelandId);
        System.out.println(actors.size());
        actors.forEach(System.out::println);
    }

    @Test
    public void saveRachelWoodSeriesToCache() {
        MoviesDbWebApi api = new MoviesDbWebApi(
                new SaverRequest(new HttpRequest())
        );

        int rachelWoodId = 38940;
        List<TvSeriesDto> series = api.actorSeries(rachelWoodId);
        for(var m : series)
            System.out.println(m);
    }

    @Test
    public void saveRecommendationsOfBreakingBadToCache() {
        MoviesDbWebApi api = new MoviesDbWebApi(
                new SaverRequest(new HttpRequest())
        );
        int BREAKING_BAD_ID = 1396;

        List<TvSeriesDto> series = api.getTvSeriesRecommendations(1, BREAKING_BAD_ID);

        System.out.println("Recomendations size: " + series.size());
        for(var m : series)
            System.out.println(m);
    }

    @Test
    public void saveHomelandSeriesByNameToCache() {
        MoviesDbWebApi api = new MoviesDbWebApi(
                new SaverRequest(new HttpRequest())
        );

        List<TvSeriesDto> series = api.searchByName(1, "Homeland");

        for(var m : series)
            System.out.println(m);
    }
}