package org.activiti.pm.request;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.query.Query;
import org.activiti.engine.query.QueryProperty;
import org.activiti.pm.filter.FilterRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Dmitry Farafonov
 *
 */
public abstract class AbstractFilterResource<T> {
  @Autowired
  protected FilterRequestFactory filterRequestFactory;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public DataResponse getFilteredList(QueryRequest<T> queryRequest, Map<String, String> allRequestParams) {
    Query query = getQuery((T) queryRequest);

    DataResponse dataResponce = createPaginateList().paginateList(allRequestParams, (PaginateRequest) queryRequest,
        query, "processInstanceId", getAllowedSortProperties());

    return dataResponce;
  }

  protected abstract Map<String, QueryProperty> getAllowedSortProperties();

  @SuppressWarnings("rawtypes")
  protected abstract Query getQuery(T queryRequest);

  protected abstract QueryPaginateList createPaginateList();
}
