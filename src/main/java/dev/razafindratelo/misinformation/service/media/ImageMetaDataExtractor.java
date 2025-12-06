package dev.razafindratelo.misinformation.service.media;

import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;

import dev.razafindratelo.misinformation.model.Image;
import dev.razafindratelo.misinformation.model.classifier.FileType;
import dev.razafindratelo.misinformation.model.classifier.ImageFormat;
import dev.razafindratelo.misinformation.model.classifier.SizeType;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
@Slf4j
public class ImageMetaDataExtractor implements MediaMetadataExtractor<Image> {

  private static final double DPI_CONSTANT = 25.4;

  @Override
  public Image apply(File file) {
    ImageInputStream iis = null;
    ImageReader reader = null;

    try {
      BufferedImage bufferedImage = ImageIO.read(file);

      if (bufferedImage == null)
        throw new IOException("Unable to read image file: " + file.getName());

      iis = ImageIO.createImageInputStream(file);
      reader = getImageReader(iis);
      IIOMetadata metadata = reader != null ? reader.getImageMetadata(0) : null;

      double dpi = metadata != null ? extractDpi(metadata) : 0.0;
      int iso = metadata != null ? extractIso(metadata) : 0;

      return Image.builder()
          .id(randomUUID().toString())
          .fileName(file.getName())
          .fileType(FileType.IMAGE)
          .size(file.length())
          .sizeType(SizeType.BYTES)
          .createdAt(now())
          .width(bufferedImage.getWidth())
          .height(bufferedImage.getHeight())
          .format(extractImageFormat(file.getName()))
          .bitDepth(bufferedImage.getColorModel().getPixelSize())
          .colorModel(bufferedImage.getColorModel().getClass().getSimpleName())
          .dpi(dpi)
          .iso(iso)
          .build();

    } catch (IOException e) {
      log.error("Error extracting metadata from image: {}", file.getAbsolutePath(), e);
      throw new UncheckedIOException(e);
    } finally {
      if (reader != null) reader.dispose();

      if (iis != null) {
        try {
          iis.close();
        } catch (IOException e) {
          log.debug("Error closing ImageInputStream", e);
        }
      }
    }
  }

  private ImageFormat extractImageFormat(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }

    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
      return null;
    }

    String extension = fileName.substring(lastDotIndex + 1);
    return ImageFormat.fromString(extension);
  }

  private ImageReader getImageReader(ImageInputStream iis) throws IOException {
    if (iis == null) return null;

    Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
    if (readers.hasNext()) {
      ImageReader reader = readers.next();
      reader.setInput(iis);
      return reader;
    }
    return null;
  }

  private double extractDpi(IIOMetadata metadata) {
    try {
      IIOMetadataNode root =
          (IIOMetadataNode) metadata.getAsTree(metadata.getNativeMetadataFormatName());
      NodeList nodes = root.getElementsByTagName("HorizontalPixelSize");

      if (nodes.getLength() > 0) {
        String pixelSize = ((IIOMetadataNode) nodes.item(0)).getAttribute("value");
        if (!pixelSize.isEmpty()) {
          double pixelSizeMM = Double.parseDouble(pixelSize);
          return DPI_CONSTANT / pixelSizeMM;
        }
      }
    } catch (Exception e) {
      log.debug("Could not extract DPI from image metadata", e);
    }
    return 0.0;
  }

  private int extractIso(IIOMetadata metadata) {
    try {
      String[] metadataFormats = metadata.getMetadataFormatNames();
      for (String format : metadataFormats) {
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(format);
        Integer iso = searchIsoInNode(root);
        if (iso != null) return iso;
      }
    } catch (Exception e) {
      log.debug("Could not extract ISO from image metadata", e);
    }
    return 0;
  }

  private Integer searchIsoInNode(IIOMetadataNode node) {
    if (node.hasAttributes()) {
      NamedNodeMap attributes = node.getAttributes();
      for (int i = 0; i < attributes.getLength(); i++) {
        Node attr = attributes.item(i);
        if (attr.getNodeName().toLowerCase().contains("iso")) {
          try {
            return Integer.parseInt(attr.getNodeValue());
          } catch (NumberFormatException e) {
            log.debug("Could not parse ISO value: {}", attr.getNodeValue());
          }
        }
      }
    }

    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i) instanceof IIOMetadataNode) {
        Integer iso = searchIsoInNode((IIOMetadataNode) children.item(i));
        if (iso != null) return iso;
      }
    }
    return null;
  }
}
