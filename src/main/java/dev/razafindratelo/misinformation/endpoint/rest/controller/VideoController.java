package dev.razafindratelo.misinformation.endpoint.rest.controller;

import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/videos")
public class VideoController {
  private final VideoService videoService;

  @GetMapping("/{userEmail}")
  public Page<Video> getAllVideoByUserEmail(
      @PathVariable String userEmail,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "size", required = false) Integer size) {
    return videoService.findAllVideosByOwnerEmail(page, size, userEmail);
  }

  @GetMapping("/{userEmail}/{videoId}")
  public Video findVideoInstanceById(@PathVariable String userEmail, @PathVariable String videoId) {
    return videoService.findById(userEmail, videoId);
  }
}
