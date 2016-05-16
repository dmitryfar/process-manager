package org.activiti.pm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.yota.vox.b2b.oms.ws.wrapper.ServiceWrapperManager;

public class ActivitiUtil {
  private static Logger logger = LoggerFactory.getLogger(ActivitiUtil.class);

  private static ProcessEngine processEngine;
  private static RuntimeService runtimeService;
  private static HistoryService historyService;
  private static RepositoryService repositoryService;
  private static ManagementService managementService;
  private static TaskService taskService;

  private static ObjectMapper mapper = new ObjectMapper();

  static {
    processEngine = ProcessEngines.getDefaultProcessEngine();
    runtimeService = processEngine.getRuntimeService();
    historyService = processEngine.getHistoryService();
    repositoryService = processEngine.getRepositoryService();
    managementService = processEngine.getManagementService();
    taskService = processEngine.getTaskService();
  }

  /**
   * Returns top process instance from execution history tree
   *
   * @param processInstanceId
   * @return
   */
  public static String getTopProcessInstanceId(String processInstanceId) {
    String topProcessInstanceId = processInstanceId;
    do {
      HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
          .processInstanceId(processInstanceId).singleResult();
      processInstanceId = historicProcessInstance.getSuperProcessInstanceId();
      if (processInstanceId != null) {
        topProcessInstanceId = processInstanceId;
      }
    } while (processInstanceId != null);

    return topProcessInstanceId;
  }

  /**
   * Gets top process instance and returns variables
   *
   * @param processInstanceId
   * @return variables map
   */
  public static Map<String, Object> getTopProcessInstanceVariables(String processInstanceId) {
    String topProcessInstanceId = getTopProcessInstanceId(processInstanceId);
    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(topProcessInstanceId).includeProcessVariables().singleResult();
    return processInstance.getProcessVariables();
  }

  /**
   * Get historic process instance tree
   *
   * @param processInstanceId
   * @return ProcessInstanceItem list
   */
  public static List<ProcessInstanceItem> getHistoricProcessInstanceTree(String processInstanceId) {
    return getProcessInstanceTree(processInstanceId, 0, false);
  }

  /**
   * Get active process instance tree using history service
   *
   * @param processInstanceId
   * @return ProcessInstanceItem list
   */
  public static List<ProcessInstanceItem> getProcessInstanceTree(String processInstanceId) {
    return getProcessInstanceTree(processInstanceId, 0, true);
  }

  private static List<ProcessInstanceItem> getProcessInstanceTree(String processInstanceId, int level, boolean activeOnly) {
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    ProcessInstanceItem processInstanceItem = null;

    List<ProcessInstanceItem> itemList = new ArrayList<ProcessInstanceItem>();
    if (activeOnly && historicProcessInstance.getEndTime() != null) {
      return itemList;
    }

    if (historicProcessInstance.getEndTime() != null) {
      processInstanceItem = new ProcessInstanceItem(historicProcessInstance, level);
    } else {
      processInstanceItem = new ProcessInstanceItem(historicProcessInstance, level);
      // processInstanceItem = new ProcessInstanceItem(processInstance, level);
    }

    itemList.add(processInstanceItem);

    // go through subprocesses
    List<HistoricProcessInstance> historicSubProcesses = historyService.createHistoricProcessInstanceQuery()
        .superProcessInstanceId(processInstanceId).list();

    for (HistoricProcessInstance subProcess : historicSubProcesses) {
      List<ProcessInstanceItem> subProcessIds = getProcessInstanceTree(subProcess.getId(), level + 1, activeOnly);
      itemList.addAll(subProcessIds);
    }

    return itemList;
  }

  public static void dumpProcessInstanceTree(List<ProcessInstanceItem> processInstanceItems) {
    StringBuffer sb = new StringBuffer();
    for (ProcessInstanceItem processInstanceItem : processInstanceItems) {
      int level = processInstanceItem.getLevel();
      String levelSpaces = (level > 0) ? String.format("%" + level * 4 + "s", "") : "";
      sb.append(levelSpaces);
      sb.append(processInstanceItem);
      sb.append("\n");
    }
    logger.info("Dump process instance tree:\n" + sb.toString());
  }

  /**
   * Returns historical activity instance flows for the process
   *
   * @param processInstanceId
   * @return flowIds list
   */
  public static List<HistoricActivityInstanceFlow> getHistoricActivityInstanceFlows(String processInstanceId) {

    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService
        .getProcessDefinition(processInstance.getProcessDefinitionId());


    List<HistoricActivityInstanceFlow> historicActivityInstanceFlows = new ArrayList<HistoricActivityInstanceFlow>();
    List<HistoricActivityInstance> historicActivityInstances = getHistoricActivityInstances(processDefinition, processInstanceId);

    // Activities and their sequence-flows, using incoming transitions:
    // For each historical activity get incoming flow. If the Source Activity of
    // the flow is present in history, add it.
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      ActivityImpl historicActivity = processDefinition.findActivity(historicActivityInstance.getActivityId());

      HistoricActivityInstanceFlow historicActivityInstanceFlow = new HistoricActivityInstanceFlow();
      historicActivityInstanceFlow.setHistoricActivityInstance(historicActivityInstance);
      historicActivityInstanceFlow.setActivity(historicActivity);

      // boolean isMultiInstance = historicActivity.getProperty("multiInstance") != null;
      String type = historicActivityInstance.getActivityType();
      List<PvmTransition> incomingTransitions = historicActivity.getIncomingTransitions();
      for (PvmTransition incomingTransition : incomingTransitions) {
        String sourceId = incomingTransition.getSource().getId();
        HistoricActivityInstance sourceHistoricActivityInstance = null;
        // Find in history with the same execution id, used for joined parallel gateways.
        // Multiple incoming paths of execution have different executionIds and
        // there is many instances of the Parallel Gateway activity in the
        // history. Because of that we should find activity in the same execution.
        // It is only for joined executions (incoming transitions count > 1)
        if (("parallelGateway".equals(type) || "inclusiveGateway".equals(type)) && incomingTransitions.size() > 1){
          sourceHistoricActivityInstance = getHistoricActivityById(historicActivityInstances, sourceId, historicActivityInstance.getExecutionId());
        } else {
          sourceHistoricActivityInstance = getHistoricActivityById(historicActivityInstances, sourceId);
        }
        if (sourceHistoricActivityInstance != null) {
          historicActivityInstanceFlow.setIncomingTransition(incomingTransition);
        }
      }
      historicActivityInstanceFlows.add(historicActivityInstanceFlow);
    }

    // TODO: check subprocess
    /*
    while (!hisActInstList.isEmpty()) {
      HistoricActivityInstance histActInst = hisActInstList.removeFirst();
      ActivityImpl activity = activityMap.get(histActInst.getActivityId());
      if (activity != null) {
        boolean isParallel = false;
        String type = histActInst.getActivityType();
        if ("parallelGateway".equals(type) || "inclusiveGateway".equals(type)){
          isParallel = true;
        } else if ("subProcess".equals(histActInst.getActivityType())){
          getHighlightedFlows(activity.getActivities(), hisActInstList, highLightedFlows);
        }

        List<PvmTransition> allOutgoingTrans = new ArrayList<PvmTransition>();
        allOutgoingTrans.addAll(activity.getOutgoingTransitions());
        allOutgoingTrans.addAll(getBoundaryEventOutgoingTransitions(activity));
        List<String> activityHighLightedFlowIds = getHighlightedFlows(allOutgoingTrans, hisActInstList, isParallel);
        highLightedFlows.addAll(activityHighLightedFlowIds);
      }
    }
    */

    return historicActivityInstanceFlows;
  }

  /**
   * Returns historical flows for the process
   *
   * @param processInstanceId
   * @return
   */
  public static List<String> getHighLightedFlows(String processInstanceId) {
    List<String> highLightedFlows = new ArrayList<String>();

    List<HistoricActivityInstanceFlow> historicActivityInstanceFlows = getHistoricActivityInstanceFlows(processInstanceId);

    for (HistoricActivityInstanceFlow historicActivityInstanceFlow : historicActivityInstanceFlows) {
      if (historicActivityInstanceFlow.getIncomingTransition() != null) {
        highLightedFlows.add(historicActivityInstanceFlow.getIncomingTransitionId());
      }
    }

    return highLightedFlows;
  }

  /**
   * Returns full list of historic activity instances. Some of activities are
   * fictive, i.e. subprocess start event
   *
   * @param processInstanceId
   * @return
   */
  public static List<HistoricActivityInstance> getHistoricActivityInstances(String processInstanceId) {
    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    // prevent NullPointerException
    if (processInstance == null) {
      return Collections.<HistoricActivityInstance>emptyList();
    }

    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService
        .getProcessDefinition(processInstance.getProcessDefinitionId());

    return getHistoricActivityInstances(processDefinition, processInstanceId);
  }

  public static List<HistoricActivityInstance> getHistoricActivityInstances(ProcessDefinitionEntity processDefinition, String processInstanceId) {
    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

    // remove duplicates historic activities:
    // Set<String> hs = new LinkedHashSet<String>();
    // hs.addAll(historicActivityIds);
    // historicActivityIds.clear();
    // historicActivityIds.addAll(hs);

    historicActivityInstances = addSubProcessStartActivities(processDefinition, historicActivityInstances);

    // ad hoc
    // History with EventBsedGateway doesn't have Events. But we can find it and
    // add a fictive one manualy by next activity from the history.
    List<HistoricActivityInstance> eventBasedActivities = getHistoricActivityInstancesByType(historicActivityInstances, "eventBasedGateway");
    for (HistoricActivityInstance eventBasedHistoricActivity : eventBasedActivities) {
      ActivityImpl eventBasedActivity = processDefinition.findActivity(eventBasedHistoricActivity.getActivityId());
      int index = historicActivityInstances.indexOf(eventBasedHistoricActivity);
      // get outgoing Event activities
      List<ActivityImpl> eventActivities = eventBasedActivity.getActivities();
      for (ActivityImpl eventActivity : eventActivities) {
        // get outgoing activities from Event
        List<ActivityImpl> nextActivities = getDestinationActivities(processDefinition, eventActivity.getId());
        // if history has next activity
        for (ActivityImpl nextActivity : nextActivities) {
          if (getHistoricActivityById(historicActivityInstances, nextActivity.getId()) != null) {
            // insert after eventBasedActivity
            HistoricActivityInstance historicActivityInstance = createHistoricActivityInstanceEntity(eventActivity);
            historicActivityInstances.add(index + 1, historicActivityInstance);
          }
        }
      }
      // check duplicates?
    }

    return historicActivityInstances;
  }

  private static HistoricActivityInstance createHistoricActivityInstanceEntity(ActivityImpl activity) {
    HistoricActivityInstanceEntity historicActivityInstance = new HistoricActivityInstanceEntity();
    historicActivityInstance.setActivityId(activity.getId());
    historicActivityInstance.setActivityName((String)activity.getProperty("name"));
    historicActivityInstance.setActivityType((String)activity.getProperty("type"));
    historicActivityInstance.setProcessDefinitionId(activity.getProcessDefinition().getId());
    return historicActivityInstance;
  }

  /**
   * Retrieve all activities and from event based activities too
   *
   * @param processDefinition
   * @return List of ActivityImpl
   */
  public static List<ActivityImpl> getProcessDefinitionActivities(ProcessDefinitionEntity processDefinition) {
    // retrieve all activities and from event based activities too
    List<ActivityImpl> activities = new ArrayList<ActivityImpl>();
    activities.addAll(processDefinition.getActivities());
    for (ActivityImpl activityImpl : processDefinition.getActivities()) {
      List<ActivityImpl> childActivities = activityImpl.getActivities();
      activities.addAll(childActivities);
    }
    return activities;
  }

  private static List<HistoricActivityInstance> addSubProcessStartActivities(ProcessDefinitionEntity processDefinition,
      List<HistoricActivityInstance> historicActivityInstances) {
    List<HistoricActivityInstance> newHistoricActivityInstances = new ArrayList<HistoricActivityInstance>();
    for (int i = 0; i < historicActivityInstances.size(); i++) {
      HistoricActivityInstance historicActivityInstance = historicActivityInstances.get(i);
      newHistoricActivityInstances.add(historicActivityInstance);
      if ("subProcess".equals(historicActivityInstance.getActivityType())) {
        ActivityImpl activity = processDefinition.findActivity(historicActivityInstance.getActivityId());
        ActivityImpl startActivity = (ActivityImpl) activity.getProperty("initial");
        HistoricActivityInstance initialHistoricActivityInstance = createHistoricActivityInstanceEntity(startActivity);
        newHistoricActivityInstances.add(initialHistoricActivityInstance );
      }
    }
    return newHistoricActivityInstances;
  }

  /**
   * Returns historic activity instance by activity id
   *
   * @param historicActivityInstances
   * @param activityId
   * @return
   */
  private static HistoricActivityInstance getHistoricActivityById(List<HistoricActivityInstance> historicActivityInstances, String activityId) {
    return getHistoricActivityById(historicActivityInstances, activityId, null);
  }

  /**
   * Returns historic activity instance by activity id and specified execution id
   *
   * @param historicActivityInstances
   * @param activityId
   * @param executionId
   * @return
   */
  private static HistoricActivityInstance getHistoricActivityById(List<HistoricActivityInstance> historicActivityInstances, String activityId, String executionId) {
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      if (historicActivityInstance.getActivityId().equals(activityId)) {
        if (executionId == null || (executionId != null && executionId.equals(historicActivityInstance.getExecutionId()))) {
          return historicActivityInstance;
        }
      }
    }
    return null;
  }

  private static List<HistoricActivityInstance> getHistoricActivityInstancesByType(List<HistoricActivityInstance> historicActivityInstances, String type) {
    List<HistoricActivityInstance> result = new ArrayList<HistoricActivityInstance>();
    for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
      if (type.equals(historicActivityInstance.getActivityType())) {
        result.add(historicActivityInstance);
      }
    }
    return result;
  }

  /**
   * Returns next activities for source activity by outgoing transitions
   *
   * @param processDefinition
   * @param sourceActivityId
   * @return
   */
  private static List<ActivityImpl> getDestinationActivities(ProcessDefinitionEntity processDefinition, String sourceActivityId) {
    List<ActivityImpl> destinationActivities = new ArrayList<ActivityImpl>();
    ActivityImpl sourceActivity = processDefinition.findActivity(sourceActivityId);
    if (sourceActivity != null) {
      List<PvmTransition> outgoingTransitions = sourceActivity.getOutgoingTransitions();
      for (PvmTransition outgoingTransition : outgoingTransitions) {
        String destinationId = outgoingTransition.getDestination().getId();
        ActivityImpl destinationActivity = processDefinition.findActivity(destinationId);
        if (destinationActivity != null) {
          destinationActivities.add(destinationActivity);
        }
      }
    }
    return destinationActivities;
  }

  /**
   * Returns prevoius activities for destination activity by incoming transitions
   *
   * @param processDefinition
   * @param destinationActivityId
   * @return
   */
  private static List<ActivityImpl> getSourceActivities(ProcessDefinitionEntity processDefinition, String destinationActivityId) {
    List<ActivityImpl> sourceActivities = new ArrayList<ActivityImpl>();
    ActivityImpl destinationActivity = processDefinition.findActivity(destinationActivityId);
    if (destinationActivity != null) {
      List<PvmTransition> incomingTransitions = destinationActivity.getIncomingTransitions();
      for (PvmTransition incomingTransition : incomingTransitions) {
        String sourceId = incomingTransition.getSource().getId();
        ActivityImpl sourceActivity = processDefinition.findActivity(sourceId);
        if (sourceActivity != null) {
          sourceActivities.add(sourceActivity);
        }
      }
    }
    return sourceActivities;
  }

  public static List<String> getActiveActivityIds(String processInstanceId) {
    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId).singleResult();

    if (processInstance.getEndTime() != null) {
      return Collections.<String>emptyList();
    }
    List<String> activityIds = runtimeService.getActiveActivityIds(processInstanceId);

    // ad hoc: if there is any parallel gateway with EndTime!=null in history
    // and there is no any next activity in history then add this parallel
    // gateway as active, waiting parallel ways

    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
        .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

    ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService
        .getProcessDefinition(processInstance.getProcessDefinitionId());

    List<HistoricActivityInstance> parallelGateways = getHistoricActivityInstancesByType(historicActivityInstances, "parallelGateway");
    for (HistoricActivityInstance parallelGateway : parallelGateways) {
      if (parallelGateway.getEndTime() != null) {
        List<ActivityImpl> prevActivities = getSourceActivities(processDefinition, parallelGateway.getActivityId());
        if (prevActivities.size() < 2) {
          continue;
        }
        List<ActivityImpl> nextActivities = getDestinationActivities(processDefinition, parallelGateway.getActivityId());
        boolean foundNextActivities = false;
        for (ActivityImpl nextActivity : nextActivities) {
          HistoricActivityInstance nextHistoricActivity = getHistoricActivityById(historicActivityInstances, nextActivity.getId());
          if (nextHistoricActivity != null) {
            foundNextActivities = true;
            break;
          }
        }
        if (!foundNextActivities) {
          activityIds.add(parallelGateway.getActivityId());
        }
      }
    }

    return activityIds;
  }

  /**
   * Gets process instance variables
   *
   * @param processInstanceId
   * @return variables map
   */
  public static Map<String, Object> getProcessInstanceVariables(String processInstanceId) {
    HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstanceId).includeProcessVariables().singleResult();
    return processInstance.getProcessVariables();
  }

  public static JSONArray getProcessInstanceTreeJSON(List<ProcessInstanceItem> processInstanceItems) {
    int currentLevel = 0;
    Map<String, Object> lastElement = null;
    List<Map<String,Object>> resultArray = new ArrayList<Map<String,Object>>();

    Map<Integer, List<Map<String,Object>>> currentLevelLists = new HashMap<Integer, List<Map<String,Object>>>();

    currentLevelLists.put(currentLevel, resultArray);

    for (ProcessInstanceItem processInstanceItem : processInstanceItems) {
      if (processInstanceItem.getLevel() > currentLevel) {
        currentLevel = processInstanceItem.getLevel();
        List<Map<String,Object>> newNodes = new ArrayList<Map<String,Object>>();
        lastElement.put("nodes", newNodes);
        currentLevelLists.put(currentLevel, newNodes);
      } else if (processInstanceItem.getLevel() < currentLevel) {
        currentLevel = processInstanceItem.getLevel();
      }
      Map<String,Object> newElement = new HashMap<String, Object>();
      newElement.put("text", getProcessInstanceTreeJSONElementText(processInstanceItem.getProcessInstance()));
      newElement.put("instanceId", processInstanceItem.getProcessInstanceId());
      currentLevelLists.get(currentLevel).add(newElement);
      lastElement = newElement;
    }

    return new JSONArray(resultArray);
  }

  private static String getProcessInstanceTreeJSONElementText(Object processInstance) {
    StringBuffer sb = new StringBuffer();
    String processDefinitionId = "";
    String processInstanceId = "";
    boolean isEnded = false;
    if (processInstance instanceof ProcessInstance) {
      processDefinitionId = ((ProcessInstance) processInstance).getProcessDefinitionId();
      processInstanceId = ((ProcessInstance) processInstance).getProcessInstanceId();
      isEnded = ((ProcessInstance) processInstance).isEnded();
    } else {
      processDefinitionId = ((HistoricProcessInstance) processInstance).getProcessDefinitionId();
      processInstanceId = ((HistoricProcessInstance) processInstance).getId();
      isEnded = ((HistoricProcessInstance) processInstance).getEndTime() != null;
    }
    sb.append("[");
    sb.append(processInstanceId);
    sb.append("] ");
    sb.append(processDefinitionId.split(":")[0]);

    if (isEnded) {
      sb.append(" (ended)");
    }
    return sb.toString();
  }

  public static List<Job> getJobs(String processInstanceId) {
    return managementService.createJobQuery().processInstanceId(processInstanceId).list();
  }

  public static String getJobExceptionStacktrace(String jobId) {
    return managementService.getJobExceptionStacktrace(jobId);
  }

  public static void restartJob(String jobId) {
    managementService.executeJob(jobId);
  }

  /**
   * Returns historic process instance
   *
   * @param processInstanceId
   * @return
   */
  public static HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
    return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
  }

  /**
   * Return process definition
   *
   * @param processDefinitionId
   * @return
   */
  public static ProcessDefinition getProcessDefinition(String processDefinitionId) {
    return repositoryService.getProcessDefinition(processDefinitionId);
  }

  /**
   * Get all order comments
   *
   * @param orderId
   * @return comments
   */
  public static List<Comment> getOrderComments(long orderId) {
    return ServiceWrapperManager.getOrderManagementService().getActivitiOrderComments(orderId);
  }

  /**
   * Get order comments for specific process instanc
   *
   * @param processInstanceId
   * @return
   */
  public static List<Comment> getProcessOrderComments(String processInstanceId) {
    return ServiceWrapperManager.getOrderManagementService().getActivitiProcessComments(processInstanceId);
  }

  public static List<Comment> getProcessInstanceComments(String processInstanceId) {
    return taskService.getProcessInstanceComments(processInstanceId);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> getCommentData(Comment comment) {
    String message = comment.getFullMessage();
    Map<String, Object> commentData;
    try {
      commentData = mapper.readValue(message, Map.class);
    } catch (Exception e) {
      commentData = new HashMap<String, Object>();
      commentData.put("message", message);
    }
    return commentData;
  }
}
