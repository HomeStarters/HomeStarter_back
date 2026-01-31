package com.dwj.homestarter.roadmap.service.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-Sent Events 서비스
 * 실시간 진행 상황 스트리밍
 */
@Slf4j
@Service
public class SseService {

    private static final long SSE_TIMEOUT = 60000L; // 60초
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * SSE Emitter 생성 및 등록
     *
     * @param taskId 작업 ID
     * @return SseEmitter
     */
    public SseEmitter createEmitter(String taskId) {
        log.info("Creating SSE emitter for task: {}", taskId);

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // 완료/타임아웃/에러 시 emitter 제거
        emitter.onCompletion(() -> {
            log.debug("SSE completed for task: {}", taskId);
            emitters.remove(taskId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout for task: {}", taskId);
            emitters.remove(taskId);
        });

        emitter.onError((e) -> {
            log.error("SSE error for task: {}", taskId, e);
            emitters.remove(taskId);
        });

        emitters.put(taskId, emitter);

        return emitter;
    }

    /**
     * 진행 상황 전송
     *
     * @param taskId 작업 ID
     * @param progress 진행률 (0-100)
     * @param message 메시지
     * @param status 상태
     */
    public void sendProgress(String taskId, int progress, String message, String status) {
        SseEmitter emitter = emitters.get(taskId);
        if (emitter == null) {
            log.debug("No emitter found for task: {}", taskId);
            return;
        }

        try {
            Map<String, Object> data = Map.of(
                    "progress", progress,
                    "message", message,
                    "status", status
            );

            emitter.send(SseEmitter.event()
                    .name("progress")
                    .data(data));

            log.debug("Progress sent for task {}: {}% - {}", taskId, progress, message);
        } catch (IOException e) {
            log.error("Failed to send progress for task: {}", taskId, e);
            emitters.remove(taskId);
        }
    }

    /**
     * 완료 이벤트 전송
     *
     * @param taskId 작업 ID
     * @param roadmapId 생성된 로드맵 ID
     */
    public void sendComplete(String taskId, String roadmapId) {
        SseEmitter emitter = emitters.get(taskId);
        if (emitter == null) {
            log.debug("No emitter found for task: {}", taskId);
            return;
        }

        try {
            Map<String, Object> data = Map.of(
                    "progress", 100,
                    "status", "COMPLETED",
                    "roadmapId", roadmapId
            );

            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(data));

            emitter.complete();
            log.info("Complete event sent for task: {}", taskId);
        } catch (IOException e) {
            log.error("Failed to send complete event for task: {}", taskId, e);
        } finally {
            emitters.remove(taskId);
        }
    }

    /**
     * 실패 이벤트 전송
     *
     * @param taskId 작업 ID
     * @param error 에러 메시지
     */
    public void sendError(String taskId, String error) {
        SseEmitter emitter = emitters.get(taskId);
        if (emitter == null) {
            log.debug("No emitter found for task: {}", taskId);
            return;
        }

        try {
            Map<String, Object> data = Map.of(
                    "progress", 0,
                    "status", "FAILED",
                    "error", error
            );

            emitter.send(SseEmitter.event()
                    .name("error")
                    .data(data));

            emitter.completeWithError(new RuntimeException(error));
            log.warn("Error event sent for task: {}", taskId);
        } catch (IOException e) {
            log.error("Failed to send error event for task: {}", taskId, e);
        } finally {
            emitters.remove(taskId);
        }
    }

    /**
     * Emitter 존재 여부 확인
     *
     * @param taskId 작업 ID
     * @return 존재 여부
     */
    public boolean hasEmitter(String taskId) {
        return emitters.containsKey(taskId);
    }
}
