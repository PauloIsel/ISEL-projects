package pt.isel.mpd.mycine_utils.requests.async;

import pt.isel.mpd.mycine_utils.resources.ResourceUtils;

import java.io.Reader;
import java.util.concurrent.CompletableFuture;

public class MockAsyncRequest implements AsyncRequest {
	
	@Override
	public CompletableFuture<Reader> getAsync(String path) {
		return CompletableFuture.supplyAsync(() -> ResourceUtils.getFromCache(path));
	}
}
