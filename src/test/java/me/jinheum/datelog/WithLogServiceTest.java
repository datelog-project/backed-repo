package me.jinheum.datelog;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.jinheum.datelog.dto.WithLogRequest;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.WithLog;
import me.jinheum.datelog.repository.UserConnectionRepository;
import me.jinheum.datelog.repository.WithLogRepository;
import me.jinheum.datelog.service.WithLogService;

@ExtendWith(MockitoExtension.class)
class WithLogServiceTest {

    @InjectMocks
    private WithLogService withLogService;

    @Mock
    private WithLogRepository withLogRepository;

    @Mock
    private UserConnectionRepository userConnectionRepository;

    @Test
    void createWithLog_shouldSaveWithLog_whenConnectionExists() {
        // given
        UUID connectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();  

        WithLogRequest request = new WithLogRequest(
        LocalDate.now(),
        "데이트 제목",
        "데이트 내용",
        BigDecimal.valueOf(37.5665),   
        BigDecimal.valueOf(126.9780),  
        8,                             
        "맑음",                         
        List.of()                      
    );

        UserConnection mockConnection = new UserConnection();
        when(userConnectionRepository.findById(connectionId)).thenReturn(Optional.of(mockConnection));

        // when
        withLogService.createWithLog(connectionId, request, userId);

        // then
        ArgumentCaptor<WithLog> captor = ArgumentCaptor.forClass(WithLog.class);
        verify(withLogRepository).save(captor.capture());

        WithLog saved = captor.getValue();
        assertThat(saved.getUserConnection()).isEqualTo(mockConnection);
        assertThat(saved.getDate()).isEqualTo(request.date());
        assertThat(saved.getPlaceName()).isEqualTo(request.placeName());
        assertThat(saved.getFeelingScore()).isEqualTo(request.feelingScore());
        assertThat(saved.getNote()).isEqualTo(request.note());
    }

    @Test
    void createWithLog_shouldThrowException_whenConnectionNotFound() {
        // given
        UUID connectionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();  
        WithLogRequest request = new WithLogRequest(
        LocalDate.now(),
        "데이트 제목",
        "데이트 내용",
        BigDecimal.valueOf(37.5665),   
        BigDecimal.valueOf(126.9780),  
        8,                             
        "맑음",                         
        List.of()                      
    );

        when(userConnectionRepository.findById(connectionId)).thenReturn(Optional.empty());

        // when + then
        assertThatThrownBy(() -> withLogService.createWithLog(connectionId, request, userId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("해당 연결이 존재하지 않습니다.");
    }
}