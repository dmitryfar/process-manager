package org.activiti.pm.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.pm.filter.variable.BooleanRestVariableConverter;
import org.activiti.pm.filter.variable.DateRestVariableConverter;
import org.activiti.pm.filter.variable.DoubleRestVariableConverter;
import org.activiti.pm.filter.variable.IntegerRestVariableConverter;
import org.activiti.pm.filter.variable.LongRestVariableConverter;
import org.activiti.pm.filter.variable.QueryVariable;
import org.activiti.pm.filter.variable.RestVariable;
import org.activiti.pm.filter.variable.RestVariable.RestVariableScope;
import org.activiti.pm.filter.variable.RestVariableConverter;
import org.activiti.pm.filter.variable.ShortRestVariableConverter;
import org.activiti.pm.filter.variable.StringRestVariableConverter;
import org.activiti.pm.request.HistoricProcessInstanceResponse;
import org.activiti.pm.request.ProcessInstanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Default implementation of a {@link FilterRequestFactory}.
 *
 * Added a new "createProcessInstanceResponse" method (with a different signature) to conditionally
 *   return the process variables that exist within the process instance when the first wait state
 *   is encountered (or when the process instance completes). Also added the population of a
 *   "completed" flag - within both the original "createProcessInstanceResponse" method and
 *   the new one with the different signature - to let the caller know whether the process
 *   instance has completed or not.
 *
 * @author Dmitry Farafonov
 */
@Component
public class FilterRequestFactory {

  @Autowired
  RepositoryService repositoryService;

  @Autowired
  HistoryService historyService;

  public static final int VARIABLE_TASK = 1;
  public static final int VARIABLE_EXECUTION = 2;
  public static final int VARIABLE_PROCESS = 3;
  public static final int VARIABLE_HISTORY_TASK = 4;
  public static final int VARIABLE_HISTORY_PROCESS = 5;
  public static final int VARIABLE_HISTORY_VARINSTANCE = 6;
  public static final int VARIABLE_HISTORY_DETAIL = 7;

  public static final String BYTE_ARRAY_VARIABLE_TYPE = "binary";
  public static final String SERIALIZABLE_VARIABLE_TYPE = "serializable";

  protected List<RestVariableConverter> variableConverters = new ArrayList<RestVariableConverter>();

  public FilterRequestFactory() {
    initializeVariableConverters();
  }


  public Object getVariableValue(QueryVariable restVariable) {
    Object value = null;

    if(restVariable.getType() != null) {
      // Try locating a converter if the type has been specified
      RestVariableConverter converter = null;
      for(RestVariableConverter conv : variableConverters) {
        if(conv.getRestTypeName().equals(restVariable.getType())) {
          converter = conv;
          break;
        }
      }
      if(converter == null) {
        throw new ActivitiIllegalArgumentException("Variable '" + restVariable.getName() + "' has unsupported type: '" + restVariable.getType() + "'.");
      }

      RestVariable temp = new RestVariable();
      temp.setValue(restVariable.getValue());
      temp.setType(restVariable.getType());
      temp.setName(restVariable.getName());
      value = converter.getVariableValue(temp);

    } else {
      // Revert to type determined by REST-to-Java mapping when no explicit type has been provided
      value = restVariable.getValue();
    }
    return value;
  }

  /**
   * @return list of {@link RestVariableConverter} which are used by this factory. Additional
   * converters can be added and existing ones replaced ore removed.
   */
  public List<RestVariableConverter> getVariableConverters() {
    return variableConverters;
  }

  /**
   * Called once when the converters need to be initialized. Override of custom conversion
   * needs to be done between java and rest.
   */
  protected void initializeVariableConverters() {
    variableConverters.add(new StringRestVariableConverter());
    variableConverters.add(new IntegerRestVariableConverter());
    variableConverters.add(new LongRestVariableConverter());
    variableConverters.add(new ShortRestVariableConverter());
    variableConverters.add(new DoubleRestVariableConverter());
    variableConverters.add(new BooleanRestVariableConverter());
    variableConverters.add(new DateRestVariableConverter());
  }


  public List<ProcessInstanceResponse> createProcessInstanceResponseList(List<ProcessInstance> processInstances) {
    // RestUrlBuilder urlBuilder = createUrlBuilder();
    List<ProcessInstanceResponse> responseList = new ArrayList<ProcessInstanceResponse>();
    for (ProcessInstance instance : processInstances) {
      responseList.add(createProcessInstanceResponse(instance));
    }
    return responseList;
  }

  public ProcessInstanceResponse createProcessInstanceResponse(ProcessInstance processInstance) {
    HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
        .processInstanceId(processInstance.getProcessInstanceId()).singleResult();

    ProcessInstanceResponse result = new ProcessInstanceResponse();
    result.setActivityId(processInstance.getActivityId());
    result.setBusinessKey(processInstance.getBusinessKey());
    result.setId(processInstance.getId());
    result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    result.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
    result.setProcessDefinitionName(processInstance.getProcessDefinitionName());
    // result.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId()));
    result.setStartTime(historicProcessInstance.getStartTime());
    result.setEnded(processInstance.isEnded());
    result.setSuspended(processInstance.isSuspended());
    // result.setUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE, processInstance.getId()));
    result.setTenantId(processInstance.getTenantId());

    //Added by Ryan Johnston
    if (processInstance.isEnded()) {
      //Process complete. Note the same in the result.
      result.setCompleted(true);
    } else {
      //Process not complete. Note the same in the result.
      result.setCompleted(false);
    }
    //End Added by Ryan Johnston

    result.setSuperProcessInstanceId(historicProcessInstance.getSuperProcessInstanceId());

    if (processInstance.getProcessVariables() != null) {
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(name, variableMap.get(name),
            RestVariableScope.LOCAL, processInstance.getId(), VARIABLE_PROCESS, false));
      }
    }

    return result;
  }

  public List<HistoricProcessInstanceResponse> createHistoricProcessInstanceResponseList(List<HistoricProcessInstance> processInstances) {
    // RestUrlBuilder urlBuilder = createUrlBuilder();
    List<HistoricProcessInstanceResponse> responseList = new ArrayList<HistoricProcessInstanceResponse>();
    for (HistoricProcessInstance instance : processInstances) {
      responseList.add(createHistoricProcessInstanceResponse(instance));
    }
    return responseList;
  }

  @SuppressWarnings("deprecation")
  public HistoricProcessInstanceResponse createHistoricProcessInstanceResponse(HistoricProcessInstance processInstance) {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();

    HistoricProcessInstanceResponse result = new HistoricProcessInstanceResponse();
    result.setBusinessKey(processInstance.getBusinessKey());
    result.setDeleteReason(processInstance.getDeleteReason());
    result.setDurationInMillis(processInstance.getDurationInMillis());
    result.setEndActivityId(processInstance.getEndActivityId());
    result.setEndTime(processInstance.getEndTime());
    result.setId(processInstance.getId());
    result.setProcessDefinitionId(processInstance.getProcessDefinitionId());
    result.setProcessDefinitionKey(processDefinition.getKey());
    result.setProcessDefinitionName(processDefinition.getName());
    //result.setProcessDefinitionUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_DEFINITION, processInstance.getProcessDefinitionId()));
    result.setStartActivityId(processInstance.getStartActivityId());
    result.setStartTime(processInstance.getStartTime());
    result.setStartUserId(processInstance.getStartUserId());
    result.setSuperProcessInstanceId(processInstance.getSuperProcessInstanceId());
    // result.setUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE, processInstance.getId()));
    if (processInstance.getProcessVariables() != null) {
      Map<String, Object> variableMap = processInstance.getProcessVariables();
      for (String name : variableMap.keySet()) {
        result.addVariable(createRestVariable(name, variableMap.get(name),
            RestVariableScope.LOCAL, processInstance.getId(), VARIABLE_HISTORY_PROCESS, false));
      }
    }
    result.setTenantId(processInstance.getTenantId());
    return result;
  }

  public RestVariable createRestVariable(String name, Object value, RestVariableScope scope,
      String id, int variableType, boolean includeBinaryValue) {

    RestVariableConverter converter = null;
    RestVariable restVar = new RestVariable();
    restVar.setVariableScope(scope);
    restVar.setName(name);

    if (value != null) {
      // Try converting the value
      for (RestVariableConverter c : variableConverters) {
        if (c.getVariableType().isAssignableFrom(value.getClass())) {
          converter = c;
          break;
        }
      }

      if (converter != null) {
        converter.convertVariableValue(value, restVar);
        restVar.setType(converter.getRestTypeName());
      } else {
        // Revert to default conversion, which is the serializable/byte-array form
        if (value instanceof Byte[] || value instanceof byte[]) {
          restVar.setType(BYTE_ARRAY_VARIABLE_TYPE);
        } else {
          restVar.setType(SERIALIZABLE_VARIABLE_TYPE);
        }

        if (includeBinaryValue) {
          restVar.setValue(value);
        }

        /*if (variableType == VARIABLE_TASK) {
          restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_TASK_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_EXECUTION) {
          restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_EXECUTION_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_PROCESS) {
          restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_PROCESS_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_TASK) {
          restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_TASK_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_PROCESS) {
          restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_PROCESS_INSTANCE_VARIABLE_DATA, id, name));
        } else if (variableType == VARIABLE_HISTORY_VARINSTANCE) {
          restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_VARIABLE_INSTANCE_DATA, id));
        } else if (variableType == VARIABLE_HISTORY_DETAIL) {
          restVar.setValueUrl(urlBuilder.buildUrl(RestUrls.URL_HISTORIC_DETAIL_VARIABLE_DATA, id));
        }*/
      }
    }
    return restVar;
  }

}