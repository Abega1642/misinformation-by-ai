package dev.razafindratelo.misinformation.model.classifier;

public enum VideoCodec {
  H264,
  H265,
  MPEG1,
  MPEG2,
  MPEG4,
  VP8,
  VP9,
  AV1,
  THEORA,
  WMV1,
  WMV2,
  WMV3,
  MJPEG,
  PRORES,
  DNXHD,
  DNXHR,
  CINEFORM,
  DV,
  XVID,
  DIVX,
  HEVC,
  FLV,
  H263,
  VC1,
  VC2,
  VC3,
  AVS,
  AVS2,
  AVS3,
  INDEO,
  CINEPAK,
  SORENSON,
  REALVIDEO,
  VP6,
  VP7,
  VP10,
  DAALA,
  THOR,
  DIRAC,
  FFV1,
  HUFFYUV,
  LAGARITH,
  UTVIDEO,
  SHEERVIDEO,
  CANOPUS,
  MSU,
  RV10,
  RV20,
  RV30,
  RV40,
  V210,
  V308,
  V408,
  V410,
  YUV4,
  APPLEPRORES,
  APPLEVIDEO,
  GO2MEETING,
  SCREENPRESSO,
  CAMTASIA,
  FRAPS,
  MSRLE,
  MSVIDEO1,
  MIMIC,
  QUICKTIME,
  ANIMATION,
  QTRLE,
  RPZA,
  SMCCVIDEO,
  TSCC,
  ULTI,
  ZLIB,
  ZMBV,
  UNKNOWN;

  public static VideoCodec fromString(String value) {
    if (value == null) return UNKNOWN;
    String v = value.trim().toUpperCase();
    return switch (v) {
      case "H.264", "H264", "AVC", "X264", "H264/AVC" -> H264;
      case "H.265", "H265", "HEVC", "X265", "H265/HEVC" -> H265;
      case "MPEG1VIDEO", "MPEG1", "MPEG-1" -> MPEG1;
      case "MPEG2VIDEO", "MPEG2", "MPEG-2" -> MPEG2;
      case "MPEG4", "MPEG-4", "MP4V", "FMP4" -> MPEG4;
      case "VP8" -> VP8;
      case "VP9" -> VP9;
      case "AV1" -> AV1;
      case "THEORA" -> THEORA;
      case "WMV1", "WMV7" -> WMV1;
      case "WMV2", "WMV8" -> WMV2;
      case "WMV3", "WMV9" -> WMV3;
      case "MJPEG", "MJPG", "MOTIONJPEG" -> MJPEG;
      case "PRORES", "APRORES" -> PRORES;
      case "DNXHD" -> DNXHD;
      case "DNXHR" -> DNXHR;
      case "CINEFORM" -> CINEFORM;
      case "DV", "DVVIDEO", "DVSD" -> DV;
      case "XVID" -> XVID;
      case "DIVX" -> DIVX;
      case "FLV", "FLV1", "FLASHVIDEO" -> FLV;
      case "H.263", "H263", "H263+" -> H263;
      case "VC1", "WVC1", "WMVA" -> VC1;
      case "VC2" -> VC2;
      case "VC3" -> VC3;
      case "AVS", "AVS1" -> AVS;
      case "AVS2" -> AVS2;
      case "AVS3" -> AVS3;
      case "INDEO", "IV31", "IV32", "IV41", "IV50" -> INDEO;
      case "CINEPAK" -> CINEPAK;
      case "SORENSON", "SVQ1", "SVQ3" -> SORENSON;
      case "REALVIDEO", "RV" -> REALVIDEO;
      case "VP6", "VP6F" -> VP6;
      case "VP7" -> VP7;
      case "VP10" -> VP10;
      case "DAALA" -> DAALA;
      case "THOR" -> THOR;
      case "DIRAC" -> DIRAC;
      case "FFV1" -> FFV1;
      case "HUFFYUV", "HFYU" -> HUFFYUV;
      case "LAGARITH", "LAGS" -> LAGARITH;
      case "UTVIDEO", "UTV" -> UTVIDEO;
      case "SHEERVIDEO" -> SHEERVIDEO;
      case "CANOPUS", "CVID" -> CANOPUS;
      case "MSU", "MSU1" -> MSU;
      case "RV10" -> RV10;
      case "RV20" -> RV20;
      case "RV30" -> RV30;
      case "RV40" -> RV40;
      case "V210" -> V210;
      case "V308" -> V308;
      case "V408" -> V408;
      case "V410" -> V410;
      case "YUV4", "YUV4MPEG" -> YUV4;
      case "APPLEVIDEO", "APVW" -> APPLEVIDEO;
      case "GO2MEETING" -> GO2MEETING;
      case "SCREENPRESSO" -> SCREENPRESSO;
      case "FRAPS", "FPS1" -> FRAPS;
      case "MSRLE", "MRLE" -> MSRLE;
      case "MSVIDEO1", "MSV1" -> MSVIDEO1;
      case "MIMIC", "MIMIC2" -> MIMIC;
      case "QUICKTIME", "QT" -> QUICKTIME;
      case "ANIMATION" -> ANIMATION;
      case "RPZA" -> RPZA;
      case "SMCCVIDEO" -> SMCCVIDEO;
      case "ULTI", "ULTIMOTION" -> ULTI;
      case "ZLIB" -> ZLIB;
      case "ZMBV" -> ZMBV;
      default -> UNKNOWN;
    };
  }
}
