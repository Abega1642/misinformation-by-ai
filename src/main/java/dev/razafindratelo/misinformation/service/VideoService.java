package dev.razafindratelo.misinformation.service;

import dev.razafindratelo.misinformation.mapper.VideoMapper;
import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.repository.VideoRepository;
import dev.razafindratelo.misinformation.service.util.Paginator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class VideoService {
  private final Paginator paginator;
  private final UserService userService;
  private final VideoRepository videoRepository;
  private final VideoMapper videoMapper;

  public Page<Video> findAllVideosByOwnerEmail(
      Integer page, Integer size, @Email @NotNull @NotBlank String email) {

    var owner = userService.findByEmail(email);
    log.info(
        "Requesting for all Videos instances of user with email={} with page={} and size={}",
        owner.email(),
        page,
        size);
    var pagination = paginator.apply(page, size);

    Pageable pageable =
        PageRequest.of(
            pagination.get("page"), pagination.get("size"), Sort.by("createdAt").descending());

    var jVideos = videoRepository.findAllByOwnerEmail(email, pageable);

    return jVideos.map(videoMapper::toCoreModel);
  }

  public Video findById(@Email @NotNull @NotBlank String email, @NotNull @NotBlank String id) {
    var jVideo =
        videoRepository
            .findById(id)
            .orElseThrow(
                () -> new EntityNotFoundException("Video instance not found with id=" + id));

    if (!jVideo.getOwner().getEmail().equals(email))
      throw new AuthorizationDeniedException("Cannot access a video instance of another user");

    log.info("Requesting Video instance of id={}", jVideo.getId());

    return videoMapper.toCoreModel(jVideo);
  }
}
