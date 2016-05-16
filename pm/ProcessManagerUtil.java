package org.activiti.pm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.pm.filter.HistoricProcessInstanceFilterResource;
import org.activiti.pm.filter.ProcessInstanceFilterResource;
import org.activiti.pm.request.AbstractFilterResource;
import org.activiti.pm.request.DataResponse;
import org.activiti.pm.request.HistoricProcessInstanceQueryRequest;
import org.activiti.pm.request.ProcessInstanceQueryRequest;
import org.activiti.pm.request.QueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ProcessManagerUtil {
  private static final Logger logger = LoggerFactory.getLogger(ProcessManagerUtil.class);

  @Autowired
  ProcessInstanceFilterResource processInstanceFilterResource;

  @Autowired
  HistoricProcessInstanceFilterResource historicProcessInstanceFilterResource;

  @Autowired
  PmI18nManager pmI18nManager;

  private ObjectMapper mapper = new ObjectMapper();
  private static final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private static ProcessManagerUtil processManagerUtil = new ProcessManagerUtil();

  public ProcessManagerUtil() {
    // mapper.setDateFormat(simpleDateFormat);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static ProcessManagerUtil getInstance() {
    return processManagerUtil;
  }

  public PmI18nManager getPmI18nManager() {
    return pmI18nManager;
  }

  public QueryRequest<?> getFilterFromRequest(HttpServletRequest request) {
    Map<String, String> allRequestParams = getRequestParameters(request);
    String filter = allRequestParams.get("filter");
    String type = allRequestParams.get("type");

    if (filter == null) {
      filter = "{}";
    }

    /*
     * BufferedReader br = new BufferedReader(new
     * InputStreamReader(request.getInputStream())); String json = ""; if (br !=
     * null) { json = br.readLine(); }
     */

    QueryRequest<?> queryFilter = null;
    try {
      if ("historic".equals(type)) {
        queryFilter = mapper.reader(HistoricProcessInstanceQueryRequest.class).readValue(filter);
      } else {
        queryFilter = mapper.readValue(filter, ProcessInstanceQueryRequest.class);
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      logger.warn("Mapper can not convert filter value " + filter + ", " + e.getMessage());
    }

    if (queryFilter == null) {
      queryFilter = ("historic".equals(type)) ?  new HistoricProcessInstanceQueryRequest() :  new ProcessInstanceQueryRequest();
    }

    return queryFilter;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public DataResponse getDataResponse(HttpServletRequest request) {
    Map<String, String> allRequestParams = getRequestParameters(request);

    QueryRequest queryRequest = getFilterFromRequest(request);

    AbstractFilterResource<?> filterResource = (queryRequest instanceof HistoricProcessInstanceQueryRequest)
        ? getHistoricProcessInstanceFilterResource()
        : getProcessInstanceFilterResource();
    return filterResource.getFilteredList(queryRequest, allRequestParams);
  }

  private AbstractFilterResource<?> getHistoricProcessInstanceFilterResource() {
    return historicProcessInstanceFilterResource;
  }
  private AbstractFilterResource<?> getProcessInstanceFilterResource() {
    return processInstanceFilterResource;
  }

  private Map<String, String> getRequestParameters(HttpServletRequest request) {
    Map<String, String> allRequestParams = new HashMap<String, String>();
    @SuppressWarnings("unchecked")
    Map<String, Object> map = request.getParameterMap();
    for (String name : map.keySet()) {
      allRequestParams.put(name, request.getParameter(name));
    }
    return allRequestParams;
  }

  public String convertToJson(Object data) {
    try {
      // Convert object to JSON string and pretty print
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
    } catch (JsonProcessingException e) {
      logger.error(e.getMessage(), e);
    }
    return "{}";
  }
}
