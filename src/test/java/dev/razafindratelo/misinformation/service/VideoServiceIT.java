package dev.razafindratelo.misinformation.service;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.razafindratelo.misinformation.conf.FacadeIT;
import dev.razafindratelo.misinformation.endpoint.rest.controller.model.UserRequest;
import dev.razafindratelo.misinformation.mapper.VideoMapper;
import dev.razafindratelo.misinformation.model.User;
import dev.razafindratelo.misinformation.model.Video;
import dev.razafindratelo.misinformation.model.classifier.AudioCodec;
import dev.razafindratelo.misinformation.model.classifier.ContainerFormat;
import dev.razafindratelo.misinformation.model.classifier.FileType;
import dev.razafindratelo.misinformation.model.classifier.SizeType;
import dev.razafindratelo.misinformation.model.classifier.VideoCodec;
import dev.razafindratelo.misinformation.repository.UserRepository;
import dev.razafindratelo.misinformation.repository.VideoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

public class VideoServiceIT extends FacadeIT {

  private static final String TEST_EMAIL = "test@example.com";
  private static final String TEST_FULL_NAME = "test full name";
  private static final String CLERK_ID = randomUUID().toString();
  private static final String EMAIL_1 = "user1@example.com";
  private static final String EMAIL_2 = "user2@example.com";
  private static final String CLERK_1 = randomUUID().toString();
  private static final String CLERK_2 = randomUUID().toString();

  @Autowired private VideoService videoService;
  @Autowired private VideoRepository videoRepository;
  @Autowired private UserService userService;
  @Autowired private UserRepository userRepository;
  @Autowired private VideoMapper videoMapper;

  @BeforeEach
  void setup() {
    videoRepository.deleteAll();
    userRepository.deleteAll();
  }

  @AfterEach
  void cleanup() {
    videoRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @Transactional
  void find_by_id_with_existing_id_succeeds() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var video = createTestVideo(user);

    var result = videoService.findById(TEST_EMAIL, video.getId());

    assertNotNull(result);
    assertEquals(video.getId(), result.getId());
    assertEquals(user.email(), result.getOwner().email());
  }

  @Test
  void find_by_id_with_nonexistent_id_throws_entity_not_found_exception() {
    var nonExistentId = randomUUID().toString();

    assertThrows(
        EntityNotFoundException.class, () -> videoService.findById(TEST_EMAIL, nonExistentId));
  }

  @Test
  void find_by_id_with_null_id_throws_validation_exception() {
    assertThrows(ConstraintViolationException.class, () -> videoService.findById(TEST_EMAIL, null));
  }

  @Test
  void find_by_id_with_blank_id_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class, () -> videoService.findById(TEST_EMAIL, "   "));
  }

  @Test
  void find_all_videos_by_owner_email_returns_empty_page_when_no_videos_exist() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);

    Page<Video> result = videoService.findAllVideosByOwnerEmail(0, 10, user.email());

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertEquals(0, result.getContent().size());
    assertTrue(result.isEmpty());
  }

  @Test
  @Transactional
  void find_all_videos_by_owner_email_returns_paginated_results() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    createTestVideo(user);
    createTestVideo(user);
    createTestVideo(user);

    Page<Video> result = videoService.findAllVideosByOwnerEmail(0, 2, user.email());

    assertNotNull(result);
    assertEquals(3, result.getTotalElements());
    assertEquals(2, result.getContent().size());
    assertEquals(2, result.getTotalPages());
  }

  @Test
  @Transactional
  void find_all_videos_by_owner_email_returns_videos_sorted_by_created_at_descending() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    var video1 = createTestVideo(user);
    sleepBriefly();
    var video2 = createTestVideo(user);
    sleepBriefly();
    var video3 = createTestVideo(user);

    Page<Video> result = videoService.findAllVideosByOwnerEmail(0, 10, user.email());

    assertNotNull(result);
    assertEquals(3, result.getContent().size());
    assertEquals(video3.getId(), result.getContent().get(0).getId());
    assertEquals(video2.getId(), result.getContent().get(1).getId());
    assertEquals(video1.getId(), result.getContent().get(2).getId());
  }

  @Test
  @Transactional
  void find_all_videos_by_owner_email_respects_page_size() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    createTestVideo(user);
    createTestVideo(user);
    createTestVideo(user);
    createTestVideo(user);
    createTestVideo(user);

    Page<Video> firstPage = videoService.findAllVideosByOwnerEmail(0, 2, user.email());
    Page<Video> secondPage = videoService.findAllVideosByOwnerEmail(1, 2, user.email());

    assertEquals(2, firstPage.getContent().size());
    assertEquals(2, secondPage.getContent().size());
    assertNotEquals(
        firstPage.getContent().getFirst().getId(), secondPage.getContent().getFirst().getId());
  }

  @Test
  void find_all_videos_by_owner_email_returns_empty_page_for_page_beyond_results() {
    var user = createTestUser(TEST_EMAIL, CLERK_ID);
    createTestVideo(user);
    createTestVideo(user);

    Page<Video> result = videoService.findAllVideosByOwnerEmail(5, 10, user.email());

    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    assertEquals(0, result.getContent().size());
  }

  @Test
  @Transactional
  void find_all_videos_by_owner_email_only_returns_videos_of_specific_user() {
    var user1 = createTestUser(EMAIL_1, CLERK_1);
    var user2 = createTestUser(EMAIL_2, CLERK_2);

    createTestVideo(user1);
    createTestVideo(user1);
    createTestVideo(user2);

    Page<Video> user1Videos = videoService.findAllVideosByOwnerEmail(0, 10, user1.email());
    Page<Video> user2Videos = videoService.findAllVideosByOwnerEmail(0, 10, user2.email());

    assertEquals(2, user1Videos.getTotalElements());
    assertEquals(1, user2Videos.getTotalElements());
  }

  @Test
  void find_all_videos_by_owner_email_with_null_email_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class,
        () -> videoService.findAllVideosByOwnerEmail(0, 10, null));
  }

  @Test
  void find_all_videos_by_owner_email_with_blank_email_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class,
        () -> videoService.findAllVideosByOwnerEmail(0, 10, "   "));
  }

  @Test
  void find_all_videos_by_owner_email_with_invalid_email_format_throws_validation_exception() {
    assertThrows(
        ConstraintViolationException.class,
        () -> videoService.findAllVideosByOwnerEmail(0, 10, "invalid-email"));
  }

  @Test
  void find_all_videos_by_owner_email_with_nonexistent_email_throws_entity_not_found_exception() {
    assertThrows(
        EntityNotFoundException.class,
        () -> videoService.findAllVideosByOwnerEmail(0, 10, "nonexistent@example.com"));
  }

  private User createTestUser(String email, String clerkId) {
    var request =
        UserRequest.builder().email(email).fullName(TEST_FULL_NAME).clerkId(clerkId).build();
    return userService.registerUser(request);
  }

  private Video createTestVideo(User owner) {
    var video =
        Video.builder()
            .id(randomUUID().toString())
            .fileName("test-video.mp4")
            .size(1024.0)
            .sizeType(SizeType.MB)
            .fileType(FileType.VIDEO)
            .createdAt(LocalDateTime.now())
            .bucketKey("test-bucket-key")
            .owner(owner)
            .duration(120.5)
            .codec(VideoCodec.H264)
            .width(1920)
            .height(1080)
            .frameRate(30.0)
            .aspectRatio("16:9")
            .containerFormat(ContainerFormat.MP4)
            .bitRate(5000000.0)
            .audioCodec(AudioCodec.AAC)
            .audioChannels(2)
            .audioSampleRate(44100)
            .build();

    var jVideo = videoMapper.toPersistenceModel(video);
    videoRepository.save(jVideo);

    return video;
  }

  private void sleepBriefly() {
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
