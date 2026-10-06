package com.tafhdev.split_bill_app.audit.service;

import com.tafhdev.split_bill_app.audit.controller.dto.AuditLogResponse;
import com.tafhdev.split_bill_app.audit.controller.mapper.AuditLogApiMapper;
import com.tafhdev.split_bill_app.audit.domain.AuditLog;
import com.tafhdev.split_bill_app.audit.repository.AuditLogRepository;
import com.tafhdev.split_bill_app.audit.service.dto.GetAuditLogCommand;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private BillGroupRepository billGroupRepository;

    @Mock
    private AuditLogApiMapper auditLogApiMapper;

    private AuditLogService auditLogService;

    private UUID groupId;
    private BillGroup group;
    private List<AuditLog> auditLogs;
    private AuditLogResponse response;

    @BeforeEach
    void setUp() {

        auditLogService = new AuditLogService(
                auditLogRepository,
                billGroupRepository,
                auditLogApiMapper
        );

        groupId = UUID.randomUUID();

        group = mock(BillGroup.class);

        auditLogs = List.of(
                mock(AuditLog.class),
                mock(AuditLog.class)
        );

        response = mock(AuditLogResponse.class);
    }

    @Test
    void shouldGetAuditLog() {

        GetAuditLogCommand command =
                new GetAuditLogCommand(groupId);

        when(billGroupRepository.findById(groupId))
                .thenReturn(java.util.Optional.of(group));

        when(auditLogRepository.findByGroupId(groupId))
                .thenReturn(auditLogs);

        when(auditLogApiMapper.toResponse(group, auditLogs))
                .thenReturn(response);

        AuditLogResponse result =
                auditLogService.getAuditLog(command);

        assertThat(result).isSameAs(response);

        verify(billGroupRepository)
                .findById(groupId);

        verify(auditLogRepository)
                .findByGroupId(groupId);

        verify(auditLogApiMapper)
                .toResponse(group, auditLogs);
    }

    @Test
    void shouldReturnEmptyAuditLogsWhenGroupHasNoAuditLogs() {

        GetAuditLogCommand command =
                new GetAuditLogCommand(groupId);

        when(billGroupRepository.findById(groupId))
                .thenReturn(java.util.Optional.of(group));

        when(auditLogRepository.findByGroupId(groupId))
                .thenReturn(List.of());

        when(auditLogApiMapper.toResponse(group, List.of()))
                .thenReturn(response);

        AuditLogResponse result =
                auditLogService.getAuditLog(command);

        assertThat(result).isSameAs(response);

        verify(billGroupRepository)
                .findById(groupId);

        verify(auditLogRepository)
                .findByGroupId(groupId);

        verify(auditLogApiMapper)
                .toResponse(group, List.of());
    }

    @Test
    void shouldRejectWhenGroupDoesNotExist() {

        GetAuditLogCommand command =
                new GetAuditLogCommand(groupId);

        when(billGroupRepository.findById(groupId))
                .thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                auditLogService.getAuditLog(command)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("group not found");

        verify(billGroupRepository)
                .findById(groupId);

        verifyNoInteractions(auditLogRepository);
        verifyNoInteractions(auditLogApiMapper);
    }
}