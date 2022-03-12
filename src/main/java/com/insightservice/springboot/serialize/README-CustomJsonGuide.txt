To modify how an object is displayed when it is returned as a response from a controller, use a JsonSerializer:
@JsonComponent
public class PathSerializer extends JsonSerializer<Path>
{
    @Override
    public void serialize(Path path, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(path.toString());
    }
}

----------------------------------------------

On the other hand, if you need to modify how an object is stored/retrieved from MongoDB, use Converters. Place these anywhere, but preferably in the same file.
IMPORTANT: You will need to add both read/write converters to the MongoConfig::customConversions() method.
@WritingConverter
enum PathWriterConverter implements Converter<Path, String>
{
    INSTANCE;

    @Override
    public String convert(Path path) {
        return path.toString();
    }
}

@ReadingConverter
enum PathReaderConverter implements Converter<String, Path>
{
    INSTANCE;

    @Override
    public Path convert(String pathString) {
        return Path.of(pathString);
    }
}