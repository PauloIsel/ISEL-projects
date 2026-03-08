package pt.isel.mpd.mycine_streams.model;

import java.util.function.Function;
import java.util.stream.Stream;

public class Genre {
	private int id;
	private String name;
	private Function<Integer, Stream<TvSeries>> seriesProvider;

	public Genre(int id, String name, Function<Integer, Stream<TvSeries>> seriesProvider) {
		this.id = id;
		this.name =  name;
		this.seriesProvider = seriesProvider;
	}

	public Stream<TvSeries> getSeries(int maxSeries) {
		return seriesProvider.apply(maxSeries);
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "{ "
			+ "name = " + name
			+ ", id = " + id
			+ " }";
	}
}
