package pt.isel.mpd.mycine_utils.requests.async;


import java.io.Reader;
import java.util.concurrent.CompletableFuture;

public class CounterAsyncRequest implements AsyncRequest {
	private final AsyncRequest request;
	private int count;
	
	public CounterAsyncRequest(AsyncRequest req) {
		this.request = req;
		this.count = 0;
	}

	@Override
	public CompletableFuture<Reader> getAsync(String path) {
		count++;
		return request.getAsync(path);
	}

	public int getCount() {
		return count;
	}
}

