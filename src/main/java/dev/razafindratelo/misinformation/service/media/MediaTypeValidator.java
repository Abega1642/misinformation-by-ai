package dev.razafindratelo.misinformation.service.media;

import dev.razafindratelo.misinformation.model.classifier.FileType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MediaTypeValidator implements ConstraintValidator<ValidMediaType, MultipartFile> {

  private static final Set<String> VIDEO_TYPES =
      Set.of(
          "video/mp4",
          "video/webm",
          "video/ogg",
          "video/quicktime",
          "video/x-msvideo",
          "video/x-matroska",
          "video/mpeg",
          "video/avi",
          "video/flv",
          "video/x-flv",
          "video/x-m4v",
          "video/3gpp",
          "video/3gpp2",
          "video/x-ms-wmv",
          "video/vnd.dlna.mpeg-tts",
          "video/vnd.mpegurl",
          "video/x-f4v",
          "video/x-ms-asf",
          "video/x-ms-wvx",
          "video/x-ms-wm",
          "video/x-mng",
          "video/x-sgi-movie",
          "video/dv",
          "video/vnd.vivo");

  private static final Set<String> IMAGE_TYPES =
      Set.of(
          "image/jpeg",
          "image/png",
          "image/gif",
          "image/bmp",
          "image/webp",
          "image/tiff",
          "image/svg+xml",
          "image/heif",
          "image/heic",
          "image/heif-sequence",
          "image/heic-sequence",
          "image/avif",
          "image/jxl",
          "image/jp2",
          "image/jpx",
          "image/jpm",
          "image/vnd.microsoft.icon",
          "image/x-icon",
          "image/vnd.adobe.photoshop",
          "image/x-exr",
          "image/vnd.radiance",
          "image/x-canon-cr2",
          "image/x-canon-cr3",
          "image/x-sony-arw",
          "image/x-nikon-nef",
          "image/x-nikon-nrw",
          "image/x-fuji-raf",
          "image/x-olympus-orf",
          "image/x-panasonic-rw2",
          "image/x-pentax-pef",
          "image/x-samsung-srw",
          "image/x-kodak-kdc",
          "image/x-kodak-dcr",
          "image/x-leaf-mos",
          "image/x-mamiya-mef",
          "image/x-epson-erf",
          "image/x-sony-srf",
          "image/x-sony-sr2",
          "image/x-bayer-bay",
          "image/x-captureone-cs1",
          "image/x-phaseone-iiq",
          "image/x-phaseone-eip",
          "image/vnd.dxf",
          "image/vnd.dwg",
          "image/eps",
          "image/x-eps",
          "image/postscript",
          "image/x-tga",
          "image/x-targa",
          "image/x-portable-anymap",
          "image/x-portable-bitmap",
          "image/x-portable-graymap",
          "image/x-portable-pixmap",
          "image/x-cmu-raster",
          "image/x-xbitmap",
          "image/x-xpixmap",
          "image/x-xwindowdump",
          "image/vnd.djvu",
          "image/vnd.fpx",
          "image/vnd.fst",
          "image/vnd.fujixerox.edmics-mmr",
          "image/vnd.fujixerox.edmics-rlc",
          "image/vnd.ms-modi",
          "image/vnd.net-fpx",
          "image/vnd.wap.wbmp",
          "image/vnd.xiff");

  private FileType category;

  @Override
  public void initialize(ValidMediaType constraintAnnotation) {
    this.category = constraintAnnotation.value();
  }

  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
    if (file == null || file.isEmpty()) return false;

    String contentType = file.getContentType();
    if (contentType == null) return false;

    boolean isValid =
        switch (category) {
          case VIDEO -> VIDEO_TYPES.contains(contentType.toLowerCase());
          case IMAGE -> IMAGE_TYPES.contains(contentType.toLowerCase());
        };

    if (!isValid) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "File must be a valid %s type. Received: %s",
                  category.name().toLowerCase(), contentType))
          .addConstraintViolation();
    }

    return isValid;
  }
}
