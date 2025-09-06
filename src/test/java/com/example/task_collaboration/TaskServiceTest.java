package com.example.task_collaboration;

import com.example.task_collaboration.application.dto.TaskRequestDTO;
import com.example.task_collaboration.application.dto.TaskResponseDTO;
import com.example.task_collaboration.domain.model.*;
import com.example.task_collaboration.domain.repository.ProjectMemberRepository;
import com.example.task_collaboration.domain.repository.TaskRepository;
import com.example.task_collaboration.domain.service.MinioService;
import com.example.task_collaboration.domain.service.ProjectService;
import com.example.task_collaboration.domain.service.TaskService;
import com.example.task_collaboration.domain.service.UserService;
import com.example.task_collaboration.infrastructure.exсeption.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private UserService userService;

    @Mock
    private MinioService minioService;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    private User currentUser;
    private Project project;
    private TaskRequestDTO taskRequestDTO;
    private UUID assigneeId;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(UUID.fromString("043ebd79-955b-44ec-bcf7-6dd21ae1c8c3"));
        currentUser.setRole(User.Role.ADMIN);

        assigneeId = UUID.randomUUID();

        project = new Project();
        project.setId(UUID.randomUUID());
        project.setCreatedBy(currentUser);
        project.setStatus(Project.Status.ACTIVE);

        taskRequestDTO = new TaskRequestDTO(
                "Test Task",
                "Test Description",
                Instant.now().plusSeconds(86400),
                "PENDING",
                null,
                assigneeId,
                project.getId()
        );
    }

    @Test
    void createTask_WithFile_Successfully() throws Exception {
        ProjectMember member = new ProjectMember(currentUser, project, ProjectMember.ProjectRole.ADMIN);

        when(projectService.findProjectByIdAndUser(project.getId(), currentUser.getId()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByUserIdAndProjectId(currentUser.getId(), project.getId()))
                .thenReturn(List.of(member));

        doReturn(Optional.of(new User() {{ setId(assigneeId); }}))
                .when(userService).findById(any(UUID.class));

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        TaskRequestDTO requestWithFile = new TaskRequestDTO(
                "Test Task",
                "Test Description",
                Instant.now().plusSeconds(86400),
                "PENDING",
                file,
                assigneeId,
                project.getId()
        );
        when(minioService.uploadFile(file)).thenReturn("/bucket/test.txt");

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            task.setCreatedAt(Instant.now());
            return task;
        });

        TaskResponseDTO result = taskService.createTask(currentUser, requestWithFile);

        assertThat(result.title(), is("Test Task"));
        assertThat(result.description(), is("Test Description"));
        assertThat(result.status(), is("PENDING"));
        assertThat(result.files(), hasSize(1));
        assertThat(result.files().get(0).name(), is("test.txt"));
        assertThat(result.files().get(0).url(), is("/bucket/test.txt"));
        assertThat(result.createdById(), is(currentUser.getId()));
        assertThat(result.assigneeId(), is(assigneeId));
        assertThat(result.projectId(), is(project.getId()));

        verify(minioService, times(1)).uploadFile(file);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void createTask_WithoutPermission_ThrowsAccessDenied() {
        User nonMemberUser = new User();
        nonMemberUser.setId(UUID.randomUUID());

        when(projectService.findProjectByIdAndUser(project.getId(), nonMemberUser.getId()))
                .thenReturn(Optional.empty());

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> taskService.createTask(nonMemberUser, taskRequestDTO)
        );

        assertThat(exception.getMessage(), is("Only project member can create tasks"));
    }

    @Test
    void createTask_WithMemberButNoAdminRole_ThrowsAccessDenied() {
        User memberUser = new User();
        memberUser.setId(UUID.randomUUID());
        ProjectMember member = new ProjectMember(memberUser, project, ProjectMember.ProjectRole.MEMBER);

        when(projectService.findProjectByIdAndUser(project.getId(), memberUser.getId()))
                .thenReturn(Optional.of(project));
        when(projectMemberRepository.findByUserIdAndProjectId(memberUser.getId(), project.getId()))
                .thenReturn(List.of(member));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> taskService.createTask(memberUser, taskRequestDTO)
        );

        assertThat(exception.getMessage(), is("Only OWNER or ADMIN can create tasks"));
    }

    @Test
    void updateTask_WithFile_Successfully() throws Exception {
        Task existingTask = new Task();
        existingTask.setId(UUID.randomUUID());
        existingTask.setTitle("Old Title");
        existingTask.setCreatedBy(currentUser);
        existingTask.setProject(project);

        ProjectMember member = new ProjectMember(currentUser, project, ProjectMember.ProjectRole.ADMIN);

        when(taskRepository.findById(existingTask.getId())).thenReturn(Optional.of(existingTask));
        when(projectMemberRepository.findByUserIdAndProjectId(currentUser.getId(), project.getId()))
                .thenReturn(List.of(member));

        MockMultipartFile file = new MockMultipartFile("file", "newfile.txt", "text/plain", "content".getBytes());
        TaskRequestDTO updateDto = new TaskRequestDTO(
                "New Title",
                "New Description",
                Instant.now().plusSeconds(86400),
                "IN_PROGRESS",
                file,
                assigneeId,
                project.getId()
        );

        when(minioService.uploadFile(file)).thenReturn("/bucket/newfile.txt");
        // Ленивый мок для любого userId, чтобы избежать ошибки User not found
        doReturn(Optional.of(new User() {{ setId(assigneeId); }}))
                .when(userService).findById(any(UUID.class));

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setStatus(Task.TaskStatus.IN_PROGRESS);
            return task;
        });

        TaskResponseDTO result = taskService.updateTask(existingTask.getId(), updateDto, currentUser.getId());

        assertThat(result.title(), is("New Title"));
        assertThat(result.status(), is("IN_PROGRESS"));
        assertThat(result.files(), hasSize(1));
        assertThat(result.files().get(0).name(), is("newfile.txt"));
        assertThat(result.files().get(0).url(), is("/bucket/newfile.txt"));

        verify(minioService, times(1)).uploadFile(file);
        verify(taskRepository, times(1)).save(existingTask);
    }
}
