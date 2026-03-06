package pt.isel.mpd.mycine_streams.model;


import java.util.stream.Stream;

public class Actor implements Comparable<Actor> {
	private int id;
	private String name;
	private double popularity;

	private Stream<TvSeries> series;

	public Actor(int id, String name, double popularity,
				 Stream<TvSeries> series) {
		this.id = id;
		this.name = name;
		this.popularity = popularity;
		this.series = series;
	}

	Stream<TvSeries> getSeries() {
		return series;
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
