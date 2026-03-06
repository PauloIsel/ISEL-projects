package pt.isel.mpd.mycine_utils.requests.async;

import pt.isel.mpd.mycine_utils.resources.ResourceUtils;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class SaverAsyncRequest implements AsyncRequest {
    private final AsyncRequest request;

    public SaverAsyncRequest(AsyncRequest request) {
        this.request = request;
    }

    @Override
    public CompletableFuture<Reader> getAsync(String path) {
        return request.getAsync(path).thenApply(reader -> {
            try (StringWriter writer = new StringWriter()) {
                reader.transferTo(writer);
                String content = writer.toString();
                ResourceUtils.saveOnCache(path, new StringReader(content));
                return new StringReader(content);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}