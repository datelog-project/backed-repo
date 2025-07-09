package me.jinheum.datelog.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.jinheum.datelog.dto.ApiResponse;
import me.jinheum.datelog.dto.WithLogRequest;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.service.WithLogService;

@Slf4j
@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
public class WithLogController {

    private final WithLogService withLogService;

    @PostMapping("{connectionId}/with-logs")
    public ResponseEntity<ApiResponse> createWithLog(@AuthenticationPrincipal UserAccount user,
                                                    @PathVariable("connectionId") UUID ConnectionId, 
                                                    @RequestBody @Valid WithLogRequest request) {
        
        log.info("요청 connectionId: {}", ConnectionId);
        log.info("로그인 userId: {}", user.getId());

        withLogService.createWithLog(ConnectionId, request, user.getId());
        return ResponseEntity.ok(new ApiResponse("게시글이 등록되었습니다."));
    }

    @DeleteMapping("/with-logs/{withLogId}")
    public ResponseEntity<ApiResponse> deleteWithLog(
            @PathVariable UUID withLogId,
            @AuthenticationPrincipal UserAccount user) {

        withLogService.deleteWithLog(withLogId, user.getId());

        return ResponseEntity.ok(new ApiResponse("게시글이 삭제되었습니다."));
    }
}