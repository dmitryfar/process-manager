package org.activiti.pm.request;

import java.util.Date;
import java.util.List;

import org.activiti.pm.filter.variable.QueryVariable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 *
 * @author Dmitry Farafonov
 *
 */
public interface IHistoricProcessInstanceQueryRequest extends QueryRequest<IHistoricProcessInstanceQueryRequest> {
  public String getProcessInstanceId();

  public List<String> getProcessInstanceIds();

  public String getProcessBusinessKey();

  public String getProcessDefinitionId();

  public String getProcessDefinitionKey();

  public String getSuperProcessInstanceId();

  public Boolean getExcludeSubprocesses();

  public Boolean getFinished();

  public String getInvolvedUser();

  public Date getFinishedAfter();

  public Date getFinishedBefore();

  public Date getStartedAfter();

  public Date getStartedBefore();

  public String getStartedBy();

  public Boolean getIncludeProcessVariables();

  @JsonTypeInfo(use = Id.CLASS, defaultImpl = QueryVariable.class)
  public List<QueryVariable> getVariables();

  public String getTenantId();

  public Boolean getWithoutTenantId();

  public String getTenantIdLike();

}
