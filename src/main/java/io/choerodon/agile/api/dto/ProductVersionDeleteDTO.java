package io.choerodon.agile.api.dto;

import javax.validation.constraints.NotNull;

/**
 * Created by jian_zhang02@163.com on 2018/6/4.
 */
public class ProductVersionDeleteDTO {
    private static final String PROJECT_ID_NULL_ERROR = "error.projectId.NotNull";
    private static final String VERSION_ID_NULL_ERROR = "error.versionId.NotNull";

    @NotNull(message = PROJECT_ID_NULL_ERROR)
    private Long projectId;
    @NotNull(message = VERSION_ID_NULL_ERROR)
    private Long versionId;
    private Long fixTargetVersionId;
    private Long influenceTargetVersionId;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public Long getFixTargetVersionId() {
        return fixTargetVersionId;
    }

    public void setFixTargetVersionId(Long fixTargetVersionId) {
        this.fixTargetVersionId = fixTargetVersionId;
    }

    public Long getInfluenceTargetVersionId() {
        return influenceTargetVersionId;
    }

    public void setInfluenceTargetVersionId(Long influenceTargetVersionId) {
        this.influenceTargetVersionId = influenceTargetVersionId;
    }
}
