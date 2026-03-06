package pt.isel.mpd.mycine_streams.model;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class TvSeries {
	private LocalDate start_date;

	private  int id;
	private  String name;
	private  double popularity;
	private Stream<Actor> actors;
	private Function<Integer, Stream<Genre>> genresProvider;
	private  List<String>  networks;
	private  Function<Integer, Stream<TvSeries>> recommendationsProvider;

	public TvSeries(LocalDate start_date,
					String name,
					int id,
					double popularity,
					List<String> networks,
					Stream<Actor> actors,
					Function<Integer, Stream<Genre>> genres,
					Function<Integer, Stream<TvSeries>> recommendations
	             ) {
		this.id = id;
		this.name = name;
		this.actors = actors;
		this.genresProvider = genres;
	    this.popularity = popularity;
	    this.start_date = start_date;
	    this.recommendationsProvider = recommendations;
		this.networks = networks;
	}
	
	public Stream<Actor> getActors() {
		return actors;
	}

	public Stream<Genre> getGenres(int maxSeries) {
		return genresProvider.apply(maxSeries);
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public double getPopularity() {
		return popularity;
	}
	
	public LocalDate getStartDate() {
		return start_date;
	}

	private Stream<String> getNetworks() {
		return  networks.stream();
	}

	public Stream<TvSeries> getRecommendations(int maxSeries) {
		return recommendationsProvider.apply(maxSeries);
	}

	public String toString() {
		return "{ "
			+ "series name=" + getName()
			+ ", start_date=" + getStartDate()
			+ ", series_id=" + id
			+ ", popularity=" + popularity
			+ ", networks=" + getNetworks ()
			+ " }";
	}
}
