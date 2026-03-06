package pt.isel.mpd.mycine_async.model;


import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Actor implements Comparable<Actor> {
	private int id;
	private String name;
	private double popularity;

	private Supplier<CompletableFuture<Stream<TvSeries>>> seriesProvider;

	public Actor(int id, String name, double popularity,
				 Supplier<CompletableFuture<Stream<TvSeries>>> series) {
		this.id = id;
		this.name = name;
		this.popularity = popularity;
		this.seriesProvider = series;
	}

	CompletableFuture<Stream<TvSeries>> getSeries() {
		return seriesProvider.get();
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

	@Override
	public String toString() {
		return "{ "
			+ "name=" + name
			+ ", id = " + id
			+ ", popularity=" + popularity
			+ " }";
	}

	@Override
	public int compareTo(Actor o) {
		return 0;
	}
}
