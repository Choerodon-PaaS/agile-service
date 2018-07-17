package io.choerodon.agile.infra.repository.impl;

import io.choerodon.agile.domain.service.IIssueService;
import io.choerodon.agile.infra.dataobject.MoveIssueDO;
import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.agile.domain.agile.entity.IssueE;
import io.choerodon.agile.domain.agile.repository.IssueRepository;
import io.choerodon.agile.infra.dataobject.IssueDO;
import io.choerodon.agile.infra.mapper.IssueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;


/**
 * 敏捷开发Issue
 *
 * @author dinghuang123@gmail.com
 * @since 2018-05-14 20:30:48
 */
@Component
@Transactional(rollbackFor = CommonException.class)
public class IssueRepositoryImpl implements IssueRepository {

    private static final String UPDATE_ERROR = "error.Issue.update";
    private static final String INSERT_ERROR = "error.Issue.create";
    private static final String DELETE_ERROR = "error.Issue.delete";

    @Autowired
    private IssueMapper issueMapper;

    @Autowired
    private IIssueService iIssueService;

    @Override
    public IssueE update(IssueE issueE, String[] fieldList) {
        IssueDO issueDO = ConvertHelper.convert(issueE, IssueDO.class);
        if (iIssueService.updateOptional(issueDO, fieldList) != 1) {
            throw new CommonException(UPDATE_ERROR);
        }
        return ConvertHelper.convert(issueMapper.selectByPrimaryKey(issueDO.getIssueId()), IssueE.class);
    }

    @Override
    public IssueE create(IssueE issueE) {
        IssueDO issueDO = ConvertHelper.convert(issueE, IssueDO.class);
        if (issueMapper.insert(issueDO) != 1) {
            throw new CommonException(INSERT_ERROR);
        }
        return ConvertHelper.convert(issueMapper.selectByPrimaryKey(issueDO.getIssueId()), IssueE.class);
    }

    @Override
    public int delete(Long projectId, Long issueId) {
        IssueDO issueDO = new IssueDO();
        issueDO.setProjectId(projectId);
        issueDO.setIssueId(issueId);
        IssueDO issueDO1 = issueMapper.selectOne(issueDO);
        int isDelete = issueMapper.delete(issueDO1);
        if (isDelete != 1) {
            throw new CommonException(DELETE_ERROR);
        }
        return isDelete;
    }

    @Override
    public Boolean removeFromSprint(Long projectId, Long sprintId) {
        issueMapper.removeFromSprint(projectId, sprintId);
        return true;
    }

    @Override
    public IssueE updateSelective(IssueE issueE) {
        IssueDO issueDO = ConvertHelper.convert(issueE, IssueDO.class);
        if (issueMapper.updateByPrimaryKeySelective(issueDO) != 1) {
            throw new CommonException("error.issue.update");
        }
        return ConvertHelper.convert(issueMapper.selectByPrimaryKey(issueDO.getIssueId()), IssueE.class);
    }

    @Override
    public Boolean batchIssueToVersion(Long projectId, Long versionId, List<Long> issueIds, Date date, Long userId) {
        issueMapper.batchIssueToVersion(projectId, versionId, issueIds, date, userId);
        return true;
    }

    @Override
    public Boolean batchIssueToEpic(Long projectId, Long epicId, List<Long> issueIds) {
        issueMapper.batchIssueToEpic(projectId, epicId, issueIds);
        return true;
    }

    @Override
    public int batchRemoveVersion(Long projectId, List<Long> issueIds) {
        return issueMapper.batchRemoveFromVersion(projectId, issueIds);
    }

    @Override
    public int batchUpdateIssueEpicId(Long projectId, Long issueId) {
        return issueMapper.batchUpdateIssueEpicId(projectId, issueId);
    }

    @Override
    public int issueToDestinationByIds(Long projectId, Long sprintId, List<Long> issueIds, Date date, Long userId) {
        return issueMapper.issueToDestinationByIds(projectId, sprintId, issueIds, date, userId);
    }

    @Override
    public int batchUpdateIssueRank(Long projectId, List<MoveIssueDO> moveIssues) {
        return issueMapper.batchUpdateIssueRank(projectId, moveIssues);
    }

    @Override
    public int removeIssueFromSprintByIssueIds(Long projectId, List<Long> issueIds) {
        return issueMapper.removeIssueFromSprintByIssueIds(projectId, issueIds);
    }

    @Override
    public int issueToSprint(Long projectId, Long sprintId, Long issueId, Date date, Long userId) {
        return issueMapper.issueToSprint(projectId, sprintId, issueId, date, userId);
    }

    @Override
    public int deleteIssueFromSprintByIssueId(Long projectId, Long issueId) {
        return issueMapper.deleteIssueFromSprintByIssueId(projectId, issueId);
    }
}