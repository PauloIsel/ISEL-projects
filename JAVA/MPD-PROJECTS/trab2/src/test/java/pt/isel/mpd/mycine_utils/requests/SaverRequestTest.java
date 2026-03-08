package pt.isel.mpd.mycine_utils.requests;

import org.junit.jupiter.api.Test;
import pt.isel.mpd.mycine_utils.resources.ResourceUtils;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class SaverRequestTest {

    private String readContent(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        reader.transferTo(writer);
        return writer.toString();
    }

    @Test
    public void testSaverRequestSavesAndCanBeUsedWithMockRequest() throws IOException {
        String testPath = "test/path";
        String testContent = "{\"key\": \"value\"}";
        Request mockRequest = new Request() {
            @Override
            public Reader get(String path) {
                return new StringReader(testContent);
            }
        };
        SaverRequest saverRequest = new SaverRequest(mockRequest);

        Reader result = saverRequest.get(testPath);

        String resultContent = readContent(result);
        assertEquals(testContent, resultContent);

        Reader cachedContent = ResourceUtils.getFromCache(testPath);
        String cachedString = readContent(cachedContent);
        assertEquals(testContent, cachedString);
    }

    @Test
    public void testSaverRequestWithMultipleRequests() throws IOException {
        String testPath1 = "test/path1";
        String testPath2 = "test/path2";
        String content1 = "{\"id\": 1}";
        String content2 = "{\"id\": 2}";
        
        Request mockRequest = new Request() {
            @Override
            public Reader get(String path) {
                return new StringReader(path.equals(testPath1) ? content1 : content2);
            }
        };
        SaverRequest saverRequest = new SaverRequest(mockRequest);

        Reader result1 = saverRequest.get(testPath1);
        Reader result2 = saverRequest.get(testPath2);

        String resultContent1 = readContent(result1);
        String resultContent2 = readContent(result2);
        
        assertEquals(content1, resultContent1);
        assertEquals(content2, resultContent2);

        Reader cached1 = ResourceUtils.getFromCache(testPath1);
        Reader cached2 = ResourceUtils.getFromCache(testPath2);
        
        assertEquals(content1, readContent(cached1));
        assertEquals(content2, readContent(cached2));
    }
} 