package org.activiti.pm.request;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.activiti.pm.filter.variable.RestVariable;
import org.activiti.pm.util.DateToStringSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Dmitry Farafonov
 */
public class HistoricProcessInstanceResponse {

  protected String id;
  protected String url;
  protected String businessKey;
  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected String processDefinitionUrl;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date startTime;
  @JsonSerialize(using = DateToStringSerializer.class, as=Date.class)
  protected Date endTime;
  protected Long durationInMillis;
  protected String startUserId;
  protected String startActivityId;
  protected String endActivityId;
  protected String deleteReason;
  protected String superProcessInstanceId;
  protected List<RestVariable> variables = new ArrayList<RestVariable>();
  protected String tenantId;

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
  public Date getEndTime() {
    return endTime;
  }
  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }
  public Long getDurationInMillis() {
    return durationInMillis;
  }
  public void setDurationInMillis(Long durationInMillis) {
    this.durationInMillis = durationInMillis;
  }
  public String getStartUserId() {
    return startUserId;
  }
  public void setStartUserId(String startUserId) {
    this.startUserId = startUserId;
  }
  public String getStartActivityId() {
    return startActivityId;
  }
  public void setStartActivityId(String startActivityId) {
    this.startActivityId = startActivityId;
  }
  public String getEndActivityId() {
    return endActivityId;
  }
  public void setEndActivityId(String endActivityId) {
    this.endActivityId = endActivityId;
  }
  public String getDeleteReason() {
    return deleteReason;
  }
  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
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
}
