package pt.isel.mpd.mycine_utils.requests;

import java.io.Reader;

public class CounterRequest implements Request {
	private final Request request;
	private int count;

	public CounterRequest(Request request) {
		this.request = request;
		this.count = 0;
	}

	@Override
	public Reader get(String path) {
		count++;
		return request.get(path);
	}

	public int getCount() {
		return count;
	}
}

