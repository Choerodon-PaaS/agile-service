package io.choerodon.agile.infra.dataobject;

/**
 * Created by jian_zhang02@163.com on 2018/5/17.
 */
public class SprintNameDO {
    private Long sprintId;
    private String sprintName;
    private Long issueId;

    public Long getSprintId() {
        return sprintId;
    }

    public void setSprintId(Long sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }
}
