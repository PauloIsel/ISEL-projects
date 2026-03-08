package pt.isel.mpd.mycine_async.model;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TvSeries {
	private LocalDate start_date;

	private int id;
	private String name;
	private double popularity;
	private Supplier<CompletableFuture<Stream<Actor>>> actorsProvider;
	private Function<Integer, CompletableFuture<Stream<Genre>>> genresProvider;
	private Function<Integer, CompletableFuture<Stream<TvSeries>>> recommendationsProvider;
	private List<String>  networks;
	

	public TvSeries(LocalDate start_date,
					String name,
					int id,
					double popularity,
					List<String> networks,
					Supplier<CompletableFuture<Stream<Actor>>> actors,
					Function<Integer, CompletableFuture<Stream<Genre>>> genres,
					Function<Integer, CompletableFuture<Stream<TvSeries>>> recommendations
	             ) {
		this.id = id;
		this.name = name;
		this.actorsProvider = actors;
		this.genresProvider = genres;
	    this.popularity = popularity;
	    this.start_date = start_date;
	    this.recommendationsProvider = recommendations;
		this.networks = networks;
	}
	
	public CompletableFuture<Stream<Actor>> getActors() {
		return actorsProvider.get();
	}

	public CompletableFuture<Stream<Genre>> getGenres(int maxSeries) {
		return genresProvider.apply(maxSeries);
	}

	public CompletableFuture<Stream<TvSeries>> getRecommendations(int maxSeries) {
		return recommendationsProvider.apply(maxSeries);
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
