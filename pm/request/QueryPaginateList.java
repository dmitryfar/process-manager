package org.activiti.pm.request;

import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.impl.AbstractQuery;
import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dmitry Farafonov
 */
public abstract class QueryPaginateList {
  private static Logger logger = LoggerFactory.getLogger(QueryPaginateList.class);

	/**
   * uses the pagination parameters form the request and makes sure to order the result and set all pagination
   * attributes for the response to render
   *
   * @param req The request containing the pagination parameters
   * @param query The query to get the paged list from
   * @param listName The name model attribute name to use for the result list
   * @param model The model to put the list and the pagination attributes in
   * @param defaultSort THe default sort column (the rest attribute) that later will be mapped to an internal engine name
   */
  @SuppressWarnings("rawtypes")
  public DataResponse paginateList(Map<String, String> requestParams, PaginateRequest paginateRequest, Query query,
      String defaultSort, Map<String, QueryProperty> properties) {

    if (paginateRequest == null) {
      paginateRequest = new PaginateRequest();
    }

    // In case pagination request is incomplete, fill with values found in URL
    // if possible
    if (paginateRequest.getOffset() == null) {
      paginateRequest.setOffset(RequestUtil.getInteger(requestParams, "offset", 0));
    }

    if (paginateRequest.getLimit() == null) {
      paginateRequest.setLimit(RequestUtil.getInteger(requestParams, "limit", 10));
    }

    if (paginateRequest.getOrder() == null) {
      paginateRequest.setOrder(requestParams.get("order"));
    }

    if (paginateRequest.getSort() == null) {
      paginateRequest.setSort(requestParams.get("sort"));
    }

    // Use defaults for paging, if not set in the PaginationRequest, nor in the URL
    Integer start = paginateRequest.getOffset();
    if(start == null || start < 0) {
      start = 0;
    }

    Integer size = paginateRequest.getLimit();
    if(size == null || size < 0) {
      size = 10;
    }

    String sort = paginateRequest.getSort();
    if(StringUtils.isEmpty(sort)) {
      sort = defaultSort;
    }

    String order = paginateRequest.getOrder();
    if(StringUtils.isEmpty(order)) {
      order = "asc";
    }

    // Sort order

    if (sort != null && !properties.isEmpty()) {
      QueryProperty qp = properties.get(sort);
      if (qp == null) {
        throw new ActivitiIllegalArgumentException("Value for param 'sort' is not valid, '" + sort + "' is not a valid property");
      }
      ((AbstractQuery) query).orderBy(qp);
      if (order.equals("asc")) {
        query.asc();
      }
      else if (order.equals("desc")) {
        query.desc();
      }
      else {
        throw new ActivitiIllegalArgumentException("Value for param 'order' is not valid : '" + order + "', must be 'asc' or 'desc'");
      }
    }

    // Get result and set pagination parameters
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    List listPage = query.listPage(start, size);
    stopWatch.split();
    List list = processList(listPage);
    stopWatch.stop();
    logger.debug("query time: "+ stopWatch.toSplitString());
    DataResponse response = new DataResponse();
    response.setStart(start);
    response.setSize(list.size());
    response.setSort(sort);
    response.setOrder(order);
    response.setTotal(query.count());
    response.setRows(list);
    return response;
  }


  /**
   * uses the pagination parameters from the request and makes sure to order the result and set all pagination
   * attributes for the response to render
   *
   * @param req The request containing the pagination parameters
   * @param query The query to get the paged list from
   * @param listName The name model attribute name to use for the result list
   * @param model The model to put the list and the pagination attributes in
   * @param defaultSort THe default sort column (the rest attribute) that later will be mapped to an internal engine name
   */
  @SuppressWarnings("rawtypes")
  public DataResponse paginateList(Map<String, String> requestParams, Query query,
      String defaultSort, Map<String, QueryProperty> properties) {
  	return paginateList(requestParams, null, query, defaultSort, properties);
  }


  @SuppressWarnings("rawtypes")
  protected abstract List processList(List list);

}
