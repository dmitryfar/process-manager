package org.activiti.pm.request;

import java.util.List;

import org.activiti.pm.filter.variable.QueryVariable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 *
 * @author Dmitry Farafonov
 *
 */
public interface IProcessInstanceQueryRequest extends QueryRequest<IProcessInstanceQueryRequest> {
  public String getProcessInstanceId();

  public List<String> getProcessInstanceIds();

  public String getProcessBusinessKey();

  public String getProcessDefinitionId();

  public String getProcessDefinitionKey();

  public String getSuperProcessInstanceId();

  public String getSubProcessInstanceId();

  public Boolean getExcludeSubprocesses();

  public String getInvolvedUser();

  public Boolean getSuspended();

  public Boolean getIncludeProcessVariables();

  @JsonTypeInfo(use = Id.CLASS, defaultImpl = QueryVariable.class)
  public List<QueryVariable> getVariables();

  public String getTenantId();

  public Boolean getWithoutTenantId();

  public String getTenantIdLike();

}
