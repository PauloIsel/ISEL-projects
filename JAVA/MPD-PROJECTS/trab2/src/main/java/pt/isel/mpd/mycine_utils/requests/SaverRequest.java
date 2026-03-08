package pt.isel.mpd.mycine_utils.requests;

import pt.isel.mpd.mycine_utils.resources.ResourceUtils;

import java.io.*;

public class SaverRequest implements Request {
    private final Request request;

    public SaverRequest(Request request) {
        this.request = request;
    }

    @Override
    public Reader get(String path) {
        try (Reader reader = request.get(path); StringWriter writer = new StringWriter()) {
            reader.transferTo(writer);
            String content = writer.toString();
            ResourceUtils.saveOnCache(path, new StringReader(content));
            return new StringReader(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}