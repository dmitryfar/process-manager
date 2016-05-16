package org.activiti.pm.request;

import java.util.List;

import org.activiti.pm.filter.variable.QueryVariable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;


/**
 * @author Frederik Heremans
 * @author Dmitry Farafonov
 *
 */
public class ProcessInstanceQueryRequest extends PaginateRequest implements IProcessInstanceQueryRequest {

  private String processInstanceId;
  private List<String> processInstanceIds;
  private String processBusinessKey;
  private String processDefinitionId;
  private String processDefinitionKey;
  private String superProcessInstanceId;
  private String subProcessInstanceId;
  private Boolean excludeSubprocesses;
  private String involvedUser;
  private Boolean suspended;
  private Boolean includeProcessVariables;
  private List<QueryVariable> variables;
  private String tenantId;
  private String tenantIdLike;
  private Boolean withoutTenantId;

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public void setProcessInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
  }

  public String getProcessBusinessKey() {
    return processBusinessKey;
  }

  public void setProcessBusinessKey(String processBusinessKey) {
    this.processBusinessKey = processBusinessKey;
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

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public String getSubProcessInstanceId() {
    return subProcessInstanceId;
  }

  public void setSubProcessInstanceId(String subProcessInstanceId) {
    this.subProcessInstanceId = subProcessInstanceId;
  }

  public Boolean getExcludeSubprocesses() {
    return excludeSubprocesses;
  }

  public void setExcludeSubprocesses(Boolean excludeSubprocesses) {
    this.excludeSubprocesses = excludeSubprocesses;
  }

  public String getInvolvedUser() {
    return involvedUser;
  }

  public void setInvolvedUser(String involvedUser) {
    this.involvedUser = involvedUser;
  }

  public Boolean getSuspended() {
    return suspended;
  }

  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  public Boolean getIncludeProcessVariables() {
    return includeProcessVariables;
  }

  public void setIncludeProcessVariables(Boolean includeProcessVariables) {
    this.includeProcessVariables = includeProcessVariables;
  }

  @JsonTypeInfo(use=Id.CLASS, defaultImpl=QueryVariable.class)
  public List<QueryVariable> getVariables() {
    return variables;
  }

  public void setVariables(List<QueryVariable> variables) {
    this.variables = variables;
  }

  public void setTenantId(String tenantId) {
	  this.tenantId = tenantId;
  }

  public String getTenantId() {
	  return tenantId;
  }

  public void setWithoutTenantId(Boolean withoutTenantId) {
	  this.withoutTenantId = withoutTenantId;
  }

  public Boolean getWithoutTenantId() {
	  return withoutTenantId;
  }

  public String getTenantIdLike() {
	  return tenantIdLike;
  }

  public void setTenantIdLike(String tenantIdLike) {
	  this.tenantIdLike = tenantIdLike;
  }
}
