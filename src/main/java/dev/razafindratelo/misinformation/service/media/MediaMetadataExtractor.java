package dev.razafindratelo.misinformation.service.media;

import java.io.File;
import java.util.function.Function;

public interface MediaMetadataExtractor<T> extends Function<File, T> {}
