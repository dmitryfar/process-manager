package org.activiti.pm;

import java.io.Serializable;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class ProcessInstanceItem implements Serializable {
  private static final long serialVersionUID = 1L;

  @JsonIgnoreProperties({"persistentState"})
  private Object processInstance;
  private int level;

  public ProcessInstanceItem(HistoricProcessInstance historicProcessInstance, int level) {
    if (historicProcessInstance == null) {
      throw new IllegalArgumentException("historicProcessInstance can not be null");
    }
    this.processInstance = historicProcessInstance;
    this.level = level;
  }

  public ProcessInstanceItem(ProcessInstance processInstance, int level) {
    if (processInstance == null) {
      throw new IllegalArgumentException("processInstance can not be null");
    }
    this.processInstance = processInstance;
    this.level = level;
  }

  public Object getProcessInstance() {
    return processInstance;
  }

  @JsonIgnore
  public String getProcessInstanceId() {
    if (processInstance instanceof ProcessInstance) {
      return ((ProcessInstance) processInstance).getId();
    } else {
      return ((HistoricProcessInstance) processInstance).getId();
    }
  }

  @JsonIgnore
  public String getSuperProcessInstanceId() {
    if (processInstance instanceof ProcessInstance) {
      return ((ProcessInstance) processInstance).getParentId();
    } else {
      return ((HistoricProcessInstance) processInstance).getSuperProcessInstanceId();
    }
  }

  @JsonIgnore
  public boolean isEnded() {
    if (processInstance instanceof ProcessInstance) {
      return ((ProcessInstance) processInstance).isEnded();
    } else {
      return ((HistoricProcessInstance) processInstance).getEndTime() != null;
    }
  }

  public int getLevel() {
    return level;
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    String processDefinitionId = "";
    String processInstanceId = "";
    if (processInstance instanceof ProcessInstance) {
      processDefinitionId = ((ProcessInstance) processInstance).getProcessDefinitionId();
      processInstanceId = ((ProcessInstance) processInstance).getProcessInstanceId();
    } else {
      processDefinitionId = ((HistoricProcessInstance) processInstance).getProcessDefinitionId();
      processInstanceId = ((HistoricProcessInstance) processInstance).getId();
    }
    sb.append("ProcessInstanceItem [");
    sb.append(processDefinitionId);
    sb.append("][");
    sb.append(processInstanceId);
    sb.append("] ");
    if (isEnded()) {
      sb.append("ended");
      // String deleteReason = ((HistoricProcessInstance)
      // processInstance).getDeleteReason();
      // if (deleteReason == null) {
      // sb.append("ended");
      // } else {
      // sb.append(deleteReason);
      // }
    }
    return sb.toString();
  }

}
