package io.choerodon.agile.api.eventhandler;

import com.alibaba.fastjson.JSONObject;
import io.choerodon.agile.api.dto.DeployStatusPayload;
import io.choerodon.agile.api.dto.IssueStatusDTO;
import io.choerodon.agile.app.service.BoardService;
import io.choerodon.agile.app.service.IssueLinkTypeService;
import io.choerodon.agile.app.service.IssueStatusService;
import io.choerodon.agile.app.service.ProjectInfoService;
import io.choerodon.agile.domain.agile.entity.TimeZoneWorkCalendarE;
import io.choerodon.agile.domain.agile.event.OrganizationCreateEventPayload;
import io.choerodon.agile.domain.agile.event.ProjectCreateAgilePayload;
import io.choerodon.agile.domain.agile.event.ProjectEvent;
import io.choerodon.agile.domain.agile.event.StatusPayload;
import io.choerodon.agile.domain.agile.repository.TimeZoneWorkCalendarRepository;
import io.choerodon.agile.infra.dataobject.TimeZoneWorkCalendarDO;
import io.choerodon.agile.infra.mapper.TimeZoneWorkCalendarMapper;
import io.choerodon.asgard.saga.annotation.SagaTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by HuangFuqiang@choerodon.io on 2018/5/22.
 * Email: fuqianghuang01@gmail.com
 */
@Component
public class AgileEventHandler {

    private static final String BOARD = "-board";

    @Autowired
    private BoardService boardService;
    @Autowired
    private ProjectInfoService projectInfoService;
    @Autowired
    private IssueLinkTypeService issueLinkTypeService;
    @Autowired
    private TimeZoneWorkCalendarMapper timeZoneWorkCalendarMapper;
    @Autowired
    private TimeZoneWorkCalendarRepository timeZoneWorkCalendarRepository;
    @Autowired
    private IssueStatusService issueStatusService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AgileEventHandler.class);

    private static final String AGILE_INIT_TIMEZONE = "agile-init-timezone";
    private static final String AGILE_INIT_PROJECT = "agile-init-project";
    private static final String STATE_MACHINE_INIT_PROJECT = "state-machine-init-project";
    private static final String STATUS_CREATE_CONSUME_ORG = "status-create-consume-org";
    private static final String IAM_CREATE_PROJECT = "iam-create-project";
    private static final String ORG_CREATE = "org-create-organization";
    private static final String PROJECT_CREATE_STATE_MACHINE = "project-create-state-machine";
    private static final String ORG_REGISTER = "org-register";
    private static final String DEPLOY_STATEMACHINE_ADD_STATUS = "deploy-statemachine-add-status";

    /**
     * 创建项目事件
     *
     * @param message message
     */
    @SagaTask(code = AGILE_INIT_PROJECT,
            description = "agile消费创建项目事件初始化项目数据",
            sagaCode = IAM_CREATE_PROJECT,
            seq = 2)
    public String handleProjectInitByConsumeSagaTask(String message) {
        ProjectEvent projectEvent = JSONObject.parseObject(message, ProjectEvent.class);
//        boardService.initBoard(projectEvent.getProjectId(), projectEvent.getProjectName() + BOARD);
        projectInfoService.initializationProjectInfo(projectEvent);
        issueLinkTypeService.initIssueLinkType(projectEvent.getProjectId());
        LOGGER.info("接受项目创建消息{}", message);
        return message;
    }


    @SagaTask(code = STATE_MACHINE_INIT_PROJECT,
            description = "agile消费创建项目事件初始化看板",
            sagaCode = PROJECT_CREATE_STATE_MACHINE,
            seq = 3)
    public String dealStateMachineInitProject(String message) {
        ProjectCreateAgilePayload projectCreateAgilePayload = JSONObject.parseObject(message, ProjectCreateAgilePayload.class);
        ProjectEvent projectEvent = projectCreateAgilePayload.getProjectEvent();
        List<StatusPayload> statusPayloads = projectCreateAgilePayload.getStatusPayloads();
        boardService.initBoard(projectEvent.getProjectId(), projectEvent.getProjectName() + BOARD, statusPayloads);
        LOGGER.info("接受接收状态服务消息{}", message);
        return message;
    }

    @SagaTask(code = STATUS_CREATE_CONSUME_ORG,
            description = "agile消费组织层创建状态",
            sagaCode = DEPLOY_STATEMACHINE_ADD_STATUS,
            seq = 4)
    public void dealStatusCreateOrg(String message) {
        DeployStatusPayload deployStatusPayload = JSONObject.parseObject(message, DeployStatusPayload.class);
        List<Long> projectIds = deployStatusPayload.getProjectIds();
        List<StatusPayload> statusPayloads = deployStatusPayload.getStatusPayloads();
        for (Long projectId: projectIds) {
            for (StatusPayload statusPayload : statusPayloads) {
                IssueStatusDTO issueStatusDTO = new IssueStatusDTO();
                issueStatusDTO.setStatusId(statusPayload.getStatusId());
                issueStatusDTO.setCategoryCode(statusPayload.getType());
                issueStatusDTO.setName(statusPayload.getStatusName());
                issueStatusDTO.setProjectId(projectId);
                issueStatusService.createStatusByStateMachine(projectId, issueStatusDTO);
            }
        }
    }

    @SagaTask(code = AGILE_INIT_TIMEZONE, sagaCode = ORG_CREATE, seq = 1, description = "接收org服务创建组织事件初始化时区")
    public String handleOrgaizationCreateByConsumeSagaTask(String message) {
        handleOrganizationInitTimeZoneSagaTask(message);
        return message;
    }

    @SagaTask(code = AGILE_INIT_TIMEZONE,
            description = "issue消费注册组织初始化数据",
            sagaCode = ORG_REGISTER,
            seq = 3)
    public String handleOrgaizationRegisterByConsumeSagaTask(String data) {
        handleOrganizationInitTimeZoneSagaTask(data);
        return data;
    }

    private void handleOrganizationInitTimeZoneSagaTask(String data) {
        OrganizationCreateEventPayload organizationCreateEventPayload = JSONObject.parseObject(data, OrganizationCreateEventPayload.class);
        Long organizationId = organizationCreateEventPayload.getId();
        TimeZoneWorkCalendarDO timeZoneWorkCalendarDO = new TimeZoneWorkCalendarDO();
        timeZoneWorkCalendarDO.setOrganizationId(organizationId);
        TimeZoneWorkCalendarDO query = timeZoneWorkCalendarMapper.selectOne(timeZoneWorkCalendarDO);
        if (query == null) {
            TimeZoneWorkCalendarE timeZoneWorkCalendarE = new TimeZoneWorkCalendarE();
            timeZoneWorkCalendarE.setAreaCode("Asia");
            timeZoneWorkCalendarE.setTimeZoneCode("Asia/Shanghai");
            timeZoneWorkCalendarE.setSaturdayWork(false);
            timeZoneWorkCalendarE.setSundayWork(false);
            timeZoneWorkCalendarE.setUseHoliday(true);
            timeZoneWorkCalendarE.setOrganizationId(organizationId);
            timeZoneWorkCalendarRepository.create(timeZoneWorkCalendarE);
        }
        LOGGER.info("接受组织创建消息{}", data);
    }

}
