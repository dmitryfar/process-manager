package org.activiti.pm.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.ProcessInstanceQueryProperty;
import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.pm.filter.variable.QueryVariable;
import org.activiti.pm.filter.variable.QueryVariable.QueryVariableOperation;
import org.activiti.pm.request.AbstractFilterResource;
import org.activiti.pm.request.IProcessInstanceQueryRequest;
import org.activiti.pm.request.QueryPaginateList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Dmitry Farafonov
 *
 */
@Component
public class ProcessInstanceFilterResource extends AbstractFilterResource<IProcessInstanceQueryRequest> {

  @Autowired
  RuntimeService runtimeService;

  protected static Map<String, QueryProperty> allowedSortProperties = new HashMap<String, QueryProperty>();

  static {
    allowedSortProperties.put("processDefinitionId", ProcessInstanceQueryProperty.PROCESS_DEFINITION_ID);
    allowedSortProperties.put("processDefinitionName", ProcessInstanceQueryProperty.PROCESS_DEFINITION_KEY);
    allowedSortProperties.put("processInstanceId", ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID);
    allowedSortProperties.put("id", ProcessInstanceQueryProperty.PROCESS_INSTANCE_ID);
    allowedSortProperties.put("tenantId", ProcessInstanceQueryProperty.TENANT_ID);
  }

  @Override
  protected Map<String, QueryProperty> getAllowedSortProperties() {
    return allowedSortProperties;
  }

  private ProcessInstanceQuery getProcessInstanceQuery(IProcessInstanceQueryRequest queryRequest) {
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // Populate query based on request
    if (queryRequest.getProcessInstanceId() != null) {
      query.processInstanceId(queryRequest.getProcessInstanceId());
    }
    if (queryRequest.getProcessInstanceIds() != null && !queryRequest.getProcessInstanceIds().isEmpty()) {
      query.processInstanceIds(new HashSet<String>(queryRequest.getProcessInstanceIds()));
    }
    if (queryRequest.getProcessDefinitionKey() != null) {
      query.processDefinitionKey(queryRequest.getProcessDefinitionKey());
    }
    if (queryRequest.getProcessDefinitionId() != null) {
      query.processDefinitionId(queryRequest.getProcessDefinitionId());
    }
    if (queryRequest.getProcessBusinessKey() != null) {
      query.processInstanceBusinessKey(queryRequest.getProcessBusinessKey());
    }
    if (queryRequest.getInvolvedUser() != null) {
      query.involvedUser(queryRequest.getInvolvedUser());
    }
    if (queryRequest.getSuspended() != null) {
      if (queryRequest.getSuspended()) {
        query.suspended();
      } else {
        query.active();
      }
    }
    if (queryRequest.getSubProcessInstanceId() != null) {
      query.subProcessInstanceId(queryRequest.getSubProcessInstanceId());
    }
    if (queryRequest.getSuperProcessInstanceId() != null) {
      query.superProcessInstanceId(queryRequest.getSuperProcessInstanceId());
    }
    if (queryRequest.getExcludeSubprocesses() != null) {
      query.excludeSubprocesses(queryRequest.getExcludeSubprocesses());
    }
    if (queryRequest.getIncludeProcessVariables() != null) {
      if (queryRequest.getIncludeProcessVariables()) {
        query.includeProcessVariables();
      }
    }
    if (queryRequest.getVariables() != null) {
      addVariables(query, queryRequest.getVariables());
    }

    if(queryRequest.getTenantId() != null) {
      query.processInstanceTenantId(queryRequest.getTenantId());
    }

    if(queryRequest.getTenantIdLike() != null) {
      query.processInstanceTenantIdLike(queryRequest.getTenantIdLike());
    }

    if(Boolean.TRUE.equals(queryRequest.getWithoutTenantId())) {
      query.processInstanceWithoutTenantId();
    }

    return query;
  }

  protected void addVariables(ProcessInstanceQuery processInstanceQuery, List<QueryVariable> variables) {
    for (QueryVariable variable : variables) {
      if (variable.getVariableOperation() == null) {
        throw new ActivitiIllegalArgumentException("Variable operation is missing for variable: " + variable.getName());
      }
      if (variable.getValue() == null) {
        throw new ActivitiIllegalArgumentException("Variable value is missing for variable: " + variable.getName());
      }

      boolean nameLess = variable.getName() == null;

      Object actualValue = filterRequestFactory.getVariableValue(variable);

      // A value-only query is only possible using equals-operator
      if (nameLess && variable.getVariableOperation() != QueryVariableOperation.EQUALS) {
        throw new ActivitiIllegalArgumentException("Value-only query (without a variable-name) is only supported when using 'equals' operation.");
      }

      switch (variable.getVariableOperation()) {

      case EQUALS:
        if (nameLess) {
          processInstanceQuery.variableValueEquals(actualValue);
        } else {
          processInstanceQuery.variableValueEquals(variable.getName(), actualValue);
        }
        break;

      case EQUALS_IGNORE_CASE:
        if (actualValue instanceof String) {
          processInstanceQuery.variableValueEqualsIgnoreCase(variable.getName(), (String) actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported when ignoring casing, but was: "
                  + actualValue.getClass().getName());
        }
        break;

      case NOT_EQUALS:
        processInstanceQuery.variableValueNotEquals(variable.getName(), actualValue);
        break;

      case NOT_EQUALS_IGNORE_CASE:
        if (actualValue instanceof String) {
          processInstanceQuery.variableValueNotEqualsIgnoreCase(variable.getName(), (String) actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported when ignoring casing, but was: "
                  + actualValue.getClass().getName());
        }
        break;

      case LIKE:
        if (actualValue instanceof String) {
          processInstanceQuery.variableValueLike(variable.getName(), (String) actualValue);
        } else {
          throw new ActivitiIllegalArgumentException("Only string variable values are supported for like, but was: "
                  + actualValue.getClass().getName());
        }
        break;

      case GREATER_THAN:
        processInstanceQuery.variableValueGreaterThan(variable.getName(), actualValue);
        break;

      case GREATER_THAN_OR_EQUALS:
        processInstanceQuery.variableValueGreaterThanOrEqual(variable.getName(), actualValue);
        break;

      case LESS_THAN:
        processInstanceQuery.variableValueLessThan(variable.getName(), actualValue);
        break;

      case LESS_THAN_OR_EQUALS:
        processInstanceQuery.variableValueLessThanOrEqual(variable.getName(), actualValue);
        break;

      default:
        throw new ActivitiIllegalArgumentException("Unsupported variable query operation: " + variable.getVariableOperation());
      }
    }
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected Query getQuery(IProcessInstanceQueryRequest queryRequest) {
    ProcessInstanceQuery query = getProcessInstanceQuery(queryRequest);
    query.includeProcessVariables();
    return query;
  }

  @Override
  protected QueryPaginateList createPaginateList() {
    return new ProcessInstancePaginateList(filterRequestFactory);
  }

}
