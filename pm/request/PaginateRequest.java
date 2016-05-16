package org.activiti.pm.request;

/**
 * Interface representing a paginated request object, use when paging is needed
 * without using URL-parameters.
 *
 * @author Dmitry Farafonov
 */
public class PaginateRequest {

  protected Integer offset;

  protected Integer limit;

  protected String sort;

  protected String order;

  public String getSort() {
    return sort;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }
}
