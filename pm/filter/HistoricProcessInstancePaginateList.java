package org.activiti.pm.filter;

import java.util.List;

import org.activiti.pm.request.QueryPaginateList;

/**
 * @author Dmitry Farafonov
 */
public class HistoricProcessInstancePaginateList extends QueryPaginateList {

  protected FilterRequestFactory filterRequestFactory;

  public HistoricProcessInstancePaginateList(FilterRequestFactory filterRequestFactory) {
    this.filterRequestFactory = filterRequestFactory;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  protected List processList(List list) {
    return filterRequestFactory.createHistoricProcessInstanceResponseList(list);
  }
}
