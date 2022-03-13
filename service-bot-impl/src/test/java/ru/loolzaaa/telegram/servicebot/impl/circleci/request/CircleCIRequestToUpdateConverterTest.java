package ru.loolzaaa.telegram.servicebot.impl.circleci.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.loolzaaa.telegram.servicebot.impl.circleci.request.pojo.*;

import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;

class CircleCIRequestToUpdateConverterTest {

    final String command = "/circleci_result";
    final String key = System.getenv("circleci_resultKey") != null ? System.getenv("circleci_resultKey") : "KEY";
    final String type = "workflow-completed with space";
    final String workflowStatus = "success with space";
    final String commitAuthor = "Author with space";
    final String projectSlug = "gh/some/project with space";
    final String workflowName = "Workflow with space";
    final String branchName = "Master with space";
    final String commitSubject = "Add some cool feature";
    final String commitHash = "EMPTY_COMMIT_HASH";
    final String jobName = "EMPTY_JOB_NAME";

    CircleCIRequest circleCIRequest;

    @BeforeEach
    void setUp() {
        circleCIRequest = new CircleCIRequest();
        circleCIRequest.setType(type);

        Workflow workflow = new Workflow();
        workflow.setStatus(workflowStatus);
        workflow.setName(workflowName);
        circleCIRequest.setWorkflow(workflow);

        Pipeline pipeline = new Pipeline();
        circleCIRequest.setPipeline(pipeline);

        VCS vcs = new VCS();
        vcs.setBranch(branchName);
        pipeline.setVcs(vcs);

        Commit commit = new Commit();
        commit.setSubject(commitSubject);
        vcs.setCommit(commit);

        Author author = new Author();
        author.setName(commitAuthor);
        commit.setAuthor(author);

        Project project = new Project();
        project.setSlug(projectSlug);
        circleCIRequest.setProject(project);
    }

    @Test
    void shouldReturnFullCorrectUpdate() {
        final String expectedMessageText = new StringJoiner(" ")
                .add(command)
                .add(key)
                .add(type.substring(0, type.indexOf(" ")))
                .add(workflowStatus.substring(0, workflowStatus.indexOf(" ")))
                .add(commitAuthor.substring(0, commitAuthor.indexOf(" ")))
                .add(projectSlug.substring(0, projectSlug.indexOf(" ")))
                .add(workflowName.substring(0, workflowName.indexOf(" ")))
                .add(branchName.substring(0, branchName.indexOf(" ")))
                .add("<<<$")
                .add(commitSubject)
                .add("$>>>")
                .add(commitHash)
                .add(jobName)
                .toString();

        Update update = CircleCIRequestToUpdateConverter.convert(circleCIRequest);

        assertThat(update).isNotNull();
        assertThat(update.getMessage()).isNotNull();
        assertThat(update.getMessage().getText())
                .isNotNull()
                .isEqualTo(expectedMessageText);
    }

    @Test
    void shouldReturnEmptyCorrectUpdate() {
        circleCIRequest.setProject(null);
        circleCIRequest.setPipeline(null);
        circleCIRequest.setWorkflow(null);
        final String expectedMessageText = new StringJoiner(" ")
                .add(command)
                .add(key)
                .add(type.substring(0, type.indexOf(" ")))
                .add("EMPTY_WORKFLOW_STATUS EMPTY_COMMIT_AUTHOR EMPTY_PROJECT_SLUG EMPTY_WORKFLOW_NAME EMPTY_BRANCH_NAME <<<$ EMPTY_COMMIT_NAME $>>> EMPTY_COMMIT_HASH EMPTY_JOB_NAME")
                .toString();

        Update update = CircleCIRequestToUpdateConverter.convert(circleCIRequest);

        assertThat(update).isNotNull();
        assertThat(update.getMessage()).isNotNull();
        assertThat(update.getMessage().getText())
                .isNotNull()
                .isEqualTo(expectedMessageText);
    }
}