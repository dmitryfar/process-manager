package org.activiti.pm.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.pm.filter.variable.RestVariable;
import org.activiti.pm.util.DateToStringSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Modified to add a "completed" flag, which lets the caller know if the process instance
 *   has run to completion without encountering a wait state or experiencing an error/
 *   exception.
 *
 * @author Dmitry Farafonov
 */
public class ProcessInstanceResponse {
  protected String id;
  protected String url;
  protected String businessKey;
  protected boolean suspended;
  protected boolean ended;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected String processDefinitionUrl;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date startTime;
  protected String activityId;
  protected String superProcessInstanceId;
  protected List<RestVariable> variables = new ArrayList<RestVariable>();
  protected String tenantId;

  //Added by Ryan Johnston
  protected boolean completed;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public void setSuspended(boolean suspended) {
    this.suspended = suspended;
  }

  public boolean isEnded() {
    return ended;
  }

  public void setEnded(boolean ended) {
    this.ended = ended;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  public String getProcessDefinitionUrl() {
    return processDefinitionUrl;
  }

  public void setProcessDefinitionUrl(String processDefinitionUrl) {
    this.processDefinitionUrl = processDefinitionUrl;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public List<RestVariable> getVariables() {
    return variables;
  }

  public void setVariables(List<RestVariable> variables) {
    this.variables = variables;
  }

  public void addVariable(RestVariable variable) {
    variables.add(variable);
  }

  public void setTenantId(String tenantId) {
	  this.tenantId = tenantId;
  }

  public String getTenantId() {
	  return tenantId;
  }

  //Added by Ryan Johnston
  public boolean isCompleted() {
	  return completed;
  }

  //Added by Ryan Johnston
  public void setCompleted(boolean completed) {
	  this.completed = completed;
  }
}
