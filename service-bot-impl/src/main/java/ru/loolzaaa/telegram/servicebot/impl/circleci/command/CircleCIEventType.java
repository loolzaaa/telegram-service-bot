package ru.loolzaaa.telegram.servicebot.impl.circleci.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CircleCIEventType {
    WORKFLOW_COMPLETED("workflow-completed"),
    JOB_COMPLETED("job-completed");

    private String type;
}
