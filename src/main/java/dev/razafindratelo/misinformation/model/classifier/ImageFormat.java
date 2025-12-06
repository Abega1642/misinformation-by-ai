package dev.razafindratelo.misinformation.model.classifier;

public enum ImageFormat {
  JPEG,
  PNG,
  GIF,
  BMP,
  TIFF,
  WEBP,
  HEIF,
  HEIC,
  AVIF,
  JP2,
  JPX,
  JXL,
  PDF,
  RAW,
  DNG,
  CR2,
  CR3,
  ARW,
  NEF,
  NRW,
  RAF,
  ORF,
  RW2,
  PEF,
  SRW,
  KDC,
  DCR,
  MOS,
  MEF,
  ERF,
  SRF,
  SR2,
  BAY,
  CS1,
  IIQ,
  EIP,
  SVG,
  EPS,
  AI,
  CDR,
  DXF,
  DWG,
  ICO,
  CUR,
  PSD,
  EXR,
  HDR,
  DICOM,
  PCX,
  TGA,
  IFF,
  WBMP,
  XBM,
  XPM,
  UNKNOWN;

  public static ImageFormat fromString(String value) {
    if (value == null) return UNKNOWN;
    String v = value.trim().toUpperCase();
    return switch (v) {
      case "JPEG", "JPG", "JPE", "JFIF", "JPF" -> JPEG;
      case "PNG" -> PNG;
      case "GIF" -> GIF;
      case "BMP", "DIB" -> BMP;
      case "TIFF", "TIF" -> TIFF;
      case "WEBP" -> WEBP;
      case "HEIF", "HEIFS", "HEICSE" -> HEIF;
      case "HEIC" -> HEIC;
      case "AVIF" -> AVIF;
      case "JP2", "JPEG2000" -> JP2;
      case "JPX" -> JPX;
      case "JXL", "JPEGXL" -> JXL;
      case "PDF", "PDF/A" -> PDF;
      case "RAW", "RAWIMAGE" -> RAW;
      case "DNG", "DIGITALNEGATIVE" -> DNG;
      case "CR2", "CRW" -> CR2;
      case "CR3" -> CR3;
      case "ARW" -> ARW;
      case "NEF" -> NEF;
      case "NRW" -> NRW;
      case "RAF" -> RAF;
      case "ORF" -> ORF;
      case "RW2" -> RW2;
      case "PEF" -> PEF;
      case "SRW" -> SRW;
      case "KDC" -> KDC;
      case "DCR" -> DCR;
      case "MOS" -> MOS;
      case "MEF" -> MEF;
      case "ERF" -> ERF;
      case "SRF" -> SRF;
      case "SR2" -> SR2;
      case "BAY" -> BAY;
      case "CS1" -> CS1;
      case "IIQ" -> IIQ;
      case "EIP" -> EIP;
      case "SVG", "SVG+XML" -> SVG;
      case "EPS", "EPSF", "EPSI" -> EPS;
      case "AI" -> AI;
      case "CDR" -> CDR;
      case "DXF" -> DXF;
      case "DWG" -> DWG;
      case "ICO" -> ICO;
      case "CUR" -> CUR;
      case "PSD" -> PSD;
      case "EXR" -> EXR;
      case "HDR", "HDRIMAGE" -> HDR;
      case "DICOM", "DCM" -> DICOM;
      case "PCX" -> PCX;
      case "TGA", "TARGA", "ICB", "VDA", "VST" -> TGA;
      case "IFF", "ILBM", "LBM" -> IFF;
      case "WBMP" -> WBMP;
      case "XBM" -> XBM;
      case "XPM" -> XPM;
      default -> UNKNOWN;
    };
  }
}
